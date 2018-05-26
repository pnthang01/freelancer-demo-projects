package org.iff.blockchain.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.iff.blockchain.model.Block;

/**
 * Created by thangpham on 23/11/2017.
 */
public class HashUtil {

    public static String hashBlockInfo(Block block) {
        return DigestUtils.sha256(block.getIndex() + block.getPreviousHash() + block.getTimestamp()).toString();
    }
}
