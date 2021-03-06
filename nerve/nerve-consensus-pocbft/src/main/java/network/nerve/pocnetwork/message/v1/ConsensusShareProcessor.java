/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package network.nerve.pocnetwork.message.v1;

import network.nerve.pocbft.model.bo.Chain;
import network.nerve.pocbft.utils.manager.ChainManager;
import network.nerve.pocnetwork.model.ConsensusKeys;
import network.nerve.pocnetwork.model.ConsensusNet;
import network.nerve.pocnetwork.model.message.ConsensusShareMsg;
import network.nerve.pocnetwork.model.message.sub.ConsensusShare;
import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.NulsHash;
import io.nuls.base.protocol.MessageProcessor;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.ArraysTool;
import network.nerve.pocnetwork.service.ConsensusNetService;
import network.nerve.pocnetwork.service.NetworkService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static network.nerve.pocnetwork.constant.NetworkCmdConstant.POC_SHARE_MESSAGE;


/**
 * 处理收到的{@link ConsensusShareMsg},共识节点信息分享
 *
 * @author lanjinsheng
 * @version 1.0
 * @date 2019-10-17
 */
@Component("ConsensusShareProcessorV1")
public class ConsensusShareProcessor implements MessageProcessor {
    @Autowired
    private ChainManager chainManager;
    Map<String, Integer> MSG_HASH_MAP = new ConcurrentHashMap<>();
    int WARNING_HASH_SIZE = 10000;
    int MAX_HASH_SIZE = 12000;
    Map<String, Integer> MSG_HASH_MAP_TEMP = new ConcurrentHashMap<>();
    @Autowired
    ConsensusNetService consensusNetService;
    @Autowired
    NetworkService networkService;

    @Override
    public String getCmd() {
        return POC_SHARE_MESSAGE;
    }

    public String getLast8ByteByNulsHash(NulsHash hash) {
        byte[] out = new byte[8];
        byte[] in = hash.getBytes();
        int copyEnd = in.length;
        System.arraycopy(in, (copyEnd - 8), out, 0, 8);
        return HexUtil.encode(out);
    }

    public boolean duplicateMsg(ConsensusShareMsg message) {
        String simpleHash = getLast8ByteByNulsHash(message.getMsgHash());
        if (null != MSG_HASH_MAP.get(simpleHash)) {
            return true;
        }
        if (MSG_HASH_MAP.size() > MAX_HASH_SIZE) {
            MSG_HASH_MAP.clear();
            MSG_HASH_MAP.putAll(MSG_HASH_MAP_TEMP);
            MSG_HASH_MAP_TEMP.clear();
        }
        if (MSG_HASH_MAP.size() > WARNING_HASH_SIZE) {
            MSG_HASH_MAP_TEMP.put(simpleHash, 1);
        }
        MSG_HASH_MAP.put(simpleHash, 1);
        return false;
    }

    @Override
    public void process(int chainId, String nodeId, String msgStr) {
        Chain chain = chainManager.getChainMap().get(chainId);
        ConsensusShareMsg message = RPCUtil.getInstanceRpcStr(msgStr, ConsensusShareMsg.class);
        if (message == null ||duplicateMsg(message)) {
            return;
        }
        String msgHash =  message.getMsgHash().toHex();
        chain.getLogger().debug("Share message,msgHash={} recv from node={}", msgHash, nodeId);
        try {
            //校验签名
            if (!SignatureUtil.validateSignture(message.getIdentityList(), message.getSign())) {
                chain.getLogger().error("msgHash={} recv from node={} validateSignture false", msgHash, nodeId);
                return;
            }
        } catch (NulsException e) {
            chain.getLogger().error("msgHash={} recv from node={} error", msgHash, nodeId);
            chain.getLogger().error(e);
        }
        ConsensusKeys consensusKeys = consensusNetService.getSelfConsensusKeys(chainId);
        if (null == consensusKeys) {
            //非共识节点
            chain.getLogger().debug("msgHash={} is not consensus node,drop msg", msgHash);
            return;
        } else {
            ConsensusShare consensusShare = message.getDecryptConsensusShare(consensusKeys.getPrivKey(), consensusKeys.getPubKey());
            if (null == consensusShare) {
                return;
            }
            //解出的包,需要判断对方是否共识节点
            ConsensusNet dbConsensusNet = consensusNetService.getConsensusNode(chainId, AddressTool.getStringAddressByBytes(AddressTool.getAddress(message.getSign().getPublicKey(), chainId)));
            if (null == dbConsensusNet) {
                //这边需要注意，此时如果共识节点列表里面还没有该节点，可能就会误判，所以必须保障 在收到消息时候，共识列表里已经存在该消息。
                chain.getLogger().error("nodeId = {} not in consensus Group", nodeId);
                return;
            }
            //进行解析 分享地址
            for (ConsensusNet consensusNet : consensusShare.getShareList()) {
                if(ArraysTool.arrayEquals(consensusKeys.getPubKey(),consensusNet.getPubKey())){
                    continue;
                }
                consensusNetService.updateConsensusNode(chain, consensusNet);
            }

        }
        chain.getLogger().debug("=====================ConsensusShareProcessor deal end");
    }
}
