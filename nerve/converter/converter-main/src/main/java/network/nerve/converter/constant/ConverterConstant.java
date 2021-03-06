package network.nerve.converter.constant;

import io.nuls.core.crypto.HexUtil;

/**
 * @author: Loki
 * @date: 2018/11/12
 */
public interface ConverterConstant {

    String CONVERTER_CMD_PATH = "network.nerve.converter.rpc.cmd";

    /**
     * 按2秒一个块 大约1天的出块数量
     */
    long DAY_BLOCKS = 30L * 60L * 24L;
    /**
     * system params
     */
    String SYS_ALLOW_NULL_ARRAY_ELEMENT = "protostuff.runtime.allow_null_array_element";
    String SYS_FILE_ENCODING = "file.encoding";

    String HETEROGENEOUS_CONFIG = "heterogeneous.json";

    String RPC_VERSION = "1.0";

    /** nonce值初始值 */
    byte[] DEFAULT_NONCE = HexUtil.decode("0000000000000000");

    int INIT_CAPACITY_8 = 8;
    int INIT_CAPACITY_4 = 4;
    int INIT_CAPACITY_2 = 2;

    /**
     * 查询等待调用组件的交易是否确认的重试次数
     */
    int CONFIRMED_VERIFY_COUNT = 3;

    /**
     * 异构链待处理队列的处理器
     */
    String CV_PENDING_THREAD = "cv_pending_thread";
    long CV_TASK_INITIALDELAY = 30;
    long CV_TASK_PERIOD = 3;
    String CV_PENDING_PROPOSAL_THREAD = "cv_pending_proposal_thread";
    /**
     * 收集交易签名队列处理器
     */
    String CV_SIGN_THREAD = "cv_sign_thread";
    long CV_SIGN_TASK_INITIALDELAY = 20;
    long CV_SIGN_TASK_PERIOD = 3;

    /**
     * 收集交易签名队列处理器
     */
    String CV_CHECK_THREAD = "cv_check_thread";
    long CV_CHECK_TASK_INITIALDELAY = 20;
    long CV_CHECK_TASK_PERIOD = 3;

    /** 统一所有链的主资产id */
    int ALL_MAIN_ASSET_ID = 1;

    int MAGIC_NUM_100 = 100;

    int MAX_CHECK_TIMES = 10;
}
