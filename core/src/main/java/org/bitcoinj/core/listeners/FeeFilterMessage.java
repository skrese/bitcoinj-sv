package org.bitcoinj.core.listeners;

import org.bitcoinj.msg.EmptyMessage;
import org.bitcoinj.params.Net;

/**
 * Created by HashEngineering on 8/11/2017.
 */
public class FeeFilterMessage extends EmptyMessage{
    public FeeFilterMessage(Net net){
        super(net);
    }
}
