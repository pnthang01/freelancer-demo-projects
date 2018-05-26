package org.iff.blockchain.process;

import org.iff.blockchain.model.Block;
import org.iff.blockchain.util.HashUtil;

import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Created by thangpham on 23/11/2017.
 */
public class BlockChainManager {

    private static BlockChainManager _instance = null;

    private synchronized static BlockChainManager _load() {
        if (null == _instance) _instance = new BlockChainManager();
        return _instance;
    }

    private ConcurrentLinkedDeque<Block> blockChain = null;

    public BlockChainManager() {
        blockChain = new ConcurrentLinkedDeque<>();
        blockChain.add(buildGenesisBlock());
    }

    private Block buildGenesisBlock() {
        Block genesisBlock = new Block().setData("This is genesis block")
                .setIndex(0)
                .setPreviousHash("empty")
                .setTimestamp(System.currentTimeMillis());
        genesisBlock.setHash(HashUtil.hashBlockInfo(genesisBlock));
        return genesisBlock;
    }


    public boolean isValidNewBlock(Block newblock, Block previousBlock) {
        if (previousBlock.getIndex() + 1 != newblock.getIndex()) return false;
        else if (previousBlock.getHash().equals(newblock.getPreviousHash())) return false;
        else if (HashUtil.hashBlockInfo(newblock).equals(newblock.getHash())) return false;
        else if (previousBlock.getTimestamp() < newblock.getTimestamp()) return false;
        return true;
    }

    public Block getLastestBlock() {
        return blockChain.getLast();
    }

    public Block generateNextBlock(String data) {
        Block previousBlock = getLastestBlock();
        Block newbornBlock = new Block()
                .setData(data)
                .setIndex(previousBlock.getIndex() + 1)
                .setTimestamp(System.currentTimeMillis())
                .setPreviousHash(previousBlock.getHash());
        String newbornHash = HashUtil.hashBlockInfo(newbornBlock);
        return newbornBlock.setHash(newbornHash);
    }

}
