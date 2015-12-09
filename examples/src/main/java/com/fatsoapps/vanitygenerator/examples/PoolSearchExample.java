package com.fatsoapps.vanitygenerator.examples;

import com.fatsoapps.vanitygenerator.core.network.GlobalNetParams;
import com.fatsoapps.vanitygenerator.core.query.Base58FormatException;
import com.fatsoapps.vanitygenerator.core.query.Query;
import com.fatsoapps.vanitygenerator.core.query.QueryPool;
import com.fatsoapps.vanitygenerator.core.search.BaseSearchListener;
import com.fatsoapps.vanitygenerator.core.search.PoolSearch;
import com.fatsoapps.vanitygenerator.core.network.Network;
import com.fatsoapps.vanitygenerator.core.tools.Utils;
import org.bitcoinj.core.ECKey;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * An example of a multi-threaded approach of searching. Each search thread has a different update amount to show an
 * example of updating each thread at a different interval as opposed to having each thread update nearly at the same
 * time.
 */
public class PoolSearchExample implements BaseSearchListener {

    private static final GlobalNetParams netParams = GlobalNetParams.get(Network.BITCOIN);

    public static void main(String[] args) throws Base58FormatException {
        new PoolSearchExample().startExample();
    }

    public void startExample() throws Base58FormatException {
        Query easyQuery = new Query.QueryBuilder("FUN").compressed(true).begins(false).matchCase(true).findUnlimited(false).build();
        Query hardQuery = new Query.QueryBuilder("FUNN").compressed(true).begins(false).matchCase(false).findUnlimited(false).build();
        System.out.println("Odds: 1/" + easyQuery.getOdds());
        System.out.println("Odds: 1/" + hardQuery.getOdds());
        QueryPool pool = QueryPool.getInstance(netParams.getNetwork(), false);
        pool.addQuery(easyQuery);
        pool.addQuery(hardQuery);
        ExecutorService service = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 4; i++) {
            PoolSearch search = new PoolSearch(this, pool, netParams);
            search.setUpdateAmount(new Random().nextInt(5000) + 1000); // We can see threads report back at different times.
            service.execute(search);
        }
        service.shutdown();
    }

    public void onAddressFound(ECKey key, GlobalNetParams netParams, long amountGenerated, long speedPerSecond, boolean isCompressed) {
        if (!isCompressed) {
            key = key.decompress();
        }
        System.out.printf("%s %s found after %d attempts.%n", isCompressed ? "[Compressed]" : "[Uncompressed]", key.toAddress(netParams), amountGenerated);
    }

    public void updateBurstGenerated(long totalGenerated, long burstGenerated, long speed) {
        System.out.printf("%d generated since last update, %d total generated. Speed: %d.%n", burstGenerated, totalGenerated, speed);
    }

    public void onTaskCompleted(long totalGenerated, long speed) {
        System.out.printf("Task completed with %d addresses generated. Average speed: %d.%n", totalGenerated, speed);
    }

}
