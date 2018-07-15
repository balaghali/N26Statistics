package com.stats.restverticle;

import com.stats.restservice.external.services.IStatisticsService;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Verticle that periodically removes the transactions that are older than 60 seconds 
 */
public class PeriodicCleanerVerticle extends AbstractVerticle{

	private IStatisticsService mStatService;
	private static final Logger logger = LoggerFactory.getLogger(PeriodicCleanerVerticle.class);
	
	/**
	 * Currently being used for junit purpose, depending on requirements we can consider
	 * custom delay at later point of time
	 */
	private int mDelay = 8000;
	private long mID;
	
	public PeriodicCleanerVerticle(IStatisticsService statisticsService ) {
		this(statisticsService , -1);
	}
	
	/**
	 * To be used in conjunction with junit tests
	 * @param statisticsService
	 * @param delay
	 */
	PeriodicCleanerVerticle(IStatisticsService statisticsService  , int delay) {
		mStatService = statisticsService;
		mDelay = delay > 0 ? delay : mDelay;
	}
	
	/**
	 * Invoked when this verticle is deployed. (Life cycle method to start the verticle) 
	 */
	@Override
	public void start() throws Exception {
		//This timer will be caller every 12 seconds - log error and ignore any exceptions so that event loop will be intact
		mID = vertx.setPeriodic(mDelay, id -> {
				try {
					logger.info("About to removeStaleTransactions");
					mStatService.removeStaleTransactions();
				}catch(Exception e) {
					logger.error("Encountered exception while removing stale transactions" +e);
				}
			});
	}
	
	/**
	 * To be used only for junit to cancel the periodic timers
	 * @return
	 */
	long getPeriodicVerticleId() {
		return mID;
	}
}
