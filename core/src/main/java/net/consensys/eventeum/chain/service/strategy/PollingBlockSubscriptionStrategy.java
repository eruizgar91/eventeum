package net.consensys.eventeum.chain.service.strategy;

import net.consensys.eventeum.dto.block.BlockDetails;
import net.consensys.eventeum.model.LatestBlock;
import net.consensys.eventeum.service.EventStoreService;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;
import rx.Subscription;

import java.util.Optional;

public class PollingBlockSubscriptionStrategy extends AbstractBlockSubscriptionStrategy<EthBlock> {

    public PollingBlockSubscriptionStrategy(Web3j web3j, String nodeName, EventStoreService eventStoreService) {
        super(web3j, nodeName, eventStoreService);
    }

    @Override
    public Subscription subscribe() {

        final Optional<LatestBlock> latestBlock = getLatestBlock();

        if (latestBlock.isPresent()) {
            final DefaultBlockParameter blockParam = DefaultBlockParameter.valueOf(latestBlock.get().getNumber());

            blockSubscription = web3j.catchUpToLatestAndSubscribeToNewBlocksObservable(blockParam, false)
                    .subscribe(block -> {
                        triggerListeners(block);
                    });

        } else {
            blockSubscription = web3j.blockObservable(false).subscribe(block -> {
                triggerListeners(block);
            });
        }

        return blockSubscription;
    }

    @Override
    BlockDetails convertToBlockDetails(EthBlock blockObject) throws Exception {
        final EthBlock.Block block = blockObject.getBlock();
        final BlockDetails blockDetails = new BlockDetails();

        blockDetails.setNumber(block.getNumber());
        blockDetails.setHash(block.getHash());
        blockDetails.setTimestamp(block.getTimestamp());
        blockDetails.setNodeName(nodeName);
        blockDetails.setNetworkId(web3j.netVersion().send().getResult());

        return blockDetails;
    }
}
