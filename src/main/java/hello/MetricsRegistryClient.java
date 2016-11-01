package hello;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

public class MetricsRegistryClient {

	private StackDriverWriter sdWriter = new StackDriverWriter();
	private static final AtomicLong requestCounter = new AtomicLong();
	private Timer timer;

	public MetricsRegistryClient() {
		System.out.println("In MetricRegistryCLient construct");
		timer = new Timer();
		timer.scheduleAtFixedRate(new FlushTask(), 0, 60 * 1000);
	}

	private static class SingletonHelper {
		private static final MetricsRegistryClient INSTANCE = new MetricsRegistryClient();
	}

	public static MetricsRegistryClient client() {
		return SingletonHelper.INSTANCE;
	}

	public void incrRequestCount() {
		requestCounter.getAndDecrement();
	}

	class FlushTask extends TimerTask {
		public void run() {
			System.out.println("In MetricRegistryCLient : FlushTask Begin "+requestCounter.get());
			long requests = requestCounter.getAndSet(0);
			System.out
					.println("Time's up! Number of requests in this minute : "
							+ requests);
			if(requests < 0) return;
			MetricDescriptor metricDescriptor = new MetricDescriptor.MetricDescriptorBuilder()
					.fromType(
							"custom.googleapis.com/service/tomcat/requests_count")
					.fromKind("GAUGE").fromResourceType("gce_instance")
					.fromValueType("INT64").createDescriptor();
			sdWriter.send(metricDescriptor, requests);
		}
	}
}
