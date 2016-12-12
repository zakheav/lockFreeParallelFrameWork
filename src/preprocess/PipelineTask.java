package preprocess;

import java.util.List;

public class PipelineTask implements Runnable {
	private List<Handler> pipeline;

	public PipelineTask(List<Handler> pipeline) {
		this.pipeline = pipeline;
	}
	
	public void run() {
		for(Handler handler : pipeline) {
			handler.preprocess();
		}
	}
}
