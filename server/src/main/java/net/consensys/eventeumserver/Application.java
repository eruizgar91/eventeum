package net.consensys.eventeumserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.consensys.eventeum.annotation.EnableEventeum;
import net.consensys.eventeum.dto.message.EventeumMessage;
import net.consensys.eventeum.integration.broadcast.blockchain.KafkaBlockchainEventBroadcaster;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.core.KafkaTemplate;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.http.HttpService;
import rx.Observable;


@SpringBootApplication
@EnableEventeum
public class Application {

    public static void main(String[] args) throws Exception {

//        Web3j web3 = Web3j.build(new HttpService());

//        ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
//        EthBlock.Block block = web3.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false).send().getBlock();
//
//        // Get blocks
//        Observable<EthBlock> o = web3.blockObservable(false);
//
//        o.subscribe(blocks -> {
//                        System.out.println(blocks.getBlock());
//        });
//        Observable<Transaction> txs = web3.transactionObservable();
//        txs.subscribe(tx ->{
//           System.out.println("New transaction");
//           System.out.println(tx.toString());
//        });

//        EthFilter filter = new EthFilter(DefaultBlockParameterName.EARLIEST,
//                DefaultBlockParameterName.LATEST, <contract-address>)
//             .addSingleTopic(...)|.addOptionalTopics(..., ...)|...;
//        web3j.ethLogFlowable(filter).subscribe(log -> {
//        });
        SpringApplication.run(Application.class, args);
    }
}
