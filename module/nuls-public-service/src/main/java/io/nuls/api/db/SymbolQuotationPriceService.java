package io.nuls.api.db;

import io.nuls.api.model.po.PageInfo;
import io.nuls.api.model.po.StackSymbolPriceInfo;
import io.nuls.api.model.po.SymbolQuotationRecordInfo;

import java.util.List;

/**
 * @Author: zhoulijun
 * @Time: 2020-03-06 12:12
 * @Description: 币种兑USDT的汇率
 */
public interface SymbolQuotationPriceService {

    String USDT = "USDT";

    void saveFinalQuotation(List<StackSymbolPriceInfo> list);

    void saveQuotation(List<StackSymbolPriceInfo> list);

    PageInfo<SymbolQuotationRecordInfo> queryQuotationList(String symbol, int pageIndex, int pageSize,long startTime,long endTime);

    /**
     * 获取最新报价
     * @param symbol
     * @return
     */
    StackSymbolPriceInfo getFreshUsdtPrice(String symbol);

    /**
     * 获取最新报价
     * @param assetChainId
     * @param assetId
     * @return
     */
    StackSymbolPriceInfo getFreshUsdtPrice(int assetChainId, int assetId);

    /**
     * 获取最新报价
     * @param symbol
     * @param currency
     * @return
     */
    StackSymbolPriceInfo getFreshPrice(String symbol, String currency);

    /**
     * 获取最新报价
     * @param assetChainId
     * @param assetId
     * @param currency
     * @return
     */
    StackSymbolPriceInfo getFreshPrice(int assetChainId, int assetId, String currency);

    /**
     * 获取所有的币种的最新报价
     * @return
     */
    List<StackSymbolPriceInfo> getAllFreshPrice();

}
