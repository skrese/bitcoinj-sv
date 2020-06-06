package org.bitcoinj.msg;

import org.bitcoinj.params.NetworkParameters;
import org.bitcoinj.core.Utils;
import org.bitcoinj.msg.protocol.*;
import org.bitcoinj.params.Net;
import org.bitcoinj.script.ScriptOpCodes;
import org.bitcoinj.script.ScriptUtil;

import java.io.ByteArrayOutputStream;
import java.util.EnumMap;

import static com.google.common.base.Preconditions.checkState;
import static org.bitcoinj.core.Coin.FIFTY_COINS;

/**
 * Placeholder class for genesis blocks to seperate Message classes from
 * NetworkParameters
 */
public class Genesis {

    private static final EnumMap<Net, Block> GENESIS_BLOCKS = new EnumMap(Net.class);

    public static Block getFor(NetworkParameters params) {
        return getFor(params.getNet());
    }

    /**
     * <p>Genesis block for this chain.</p>
     *
     * <p>The first block in every chain is a well known constant shared between all Bitcoin implemenetations. For a
     * block to be valid, it must be eventually possible to work backwards to the genesis block by following the
     * prevBlockHash pointers in the block headers.</p>
     *
     * <p>The genesis blocks for both test and main networks contain the timestamp of when they were created,
     * and a message in the coinbase transaction. It says, <i>"The Times 03/Jan/2009 Chancellor on brink of second
     * bailout for banks"</i>.</p>
     */
    public static Block getFor(Net net) {
        Block genesis = GENESIS_BLOCKS.get(net);
        if (genesis == null) {
            synchronized (Genesis.class) {
                genesis = GENESIS_BLOCKS.get(net);
                if (genesis == null) {
                    //Ensure NetworkParams has been created
                    net.ensureParams();
                    genesis = createGenesis(net);
                    configureGenesis(net, genesis);
                    GENESIS_BLOCKS.put(net, genesis);
                }
            }
        }
        return genesis;
    }

    private static Block createGenesis(Net n) {
        Block genesisBlock = DefaultMsgAccessors.newBlock(n, Block.BLOCK_VERSION_GENESIS);
        Transaction t = new Transaction(n);
        try {
            // A script containing the difficulty bits and the following message:
            //
            //   "The Times 03/Jan/2009 Chancellor on brink of second bailout for banks"
            byte[] bytes = Utils.HEX.decode
                    ("04ffff001d0104455468652054696d65732030332f4a616e2f32303039204368616e63656c6c6f72206f6e206272696e6b206f66207365636f6e64206261696c6f757420666f722062616e6b73");
            t.addInput(new TransactionInput(n, t, bytes));
            ByteArrayOutputStream scriptPubKeyBytes = new ByteArrayOutputStream();
            ScriptUtil.writeBytes(scriptPubKeyBytes, Utils.HEX.decode
                    ("04678afdb0fe5548271967f1a67130b7105cd6a828e03909a67962e0ea1f61deb649f6bc3f4cef38c4f35504e51ec112de5c384df7ba0b8d578a4c702b6bf11d5f"));
            scriptPubKeyBytes.write(ScriptOpCodes.OP_CHECKSIG);
            t.addOutput(new TransactionOutput(n, t, FIFTY_COINS, scriptPubKeyBytes.toByteArray()));
        } catch (Exception e) {
            // Cannot happen.
            throw new RuntimeException(e);
        }
        genesisBlock.addTransaction(t);
        return genesisBlock;
    }

    private static void configureGenesis(Net net, Block genesis) {
        NetworkParameters params = net.params();
        genesis.setDifficultyTarget(params.genesisDifficulty());
        genesis.setTime(params.genesisTime());
        genesis.setNonce(params.genesisNonce());
        if (net == Net.UNITTEST) {
            genesis.solve();
        } else {
            String genesisHash = genesis.getHashAsString();
            boolean genesisHashCorrect = genesisHash.equals(params.genesisHash());
            checkState(genesisHashCorrect);
        }
    }

}
