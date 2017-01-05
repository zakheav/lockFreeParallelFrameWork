package preprocess;

import java.util.List;

public class PipelineTask implements Runnable {
	private List<Handler> pipeline;
	private Object params;
	public PipelineTask(List<Handler> pipeline, Object params) {
		this.pipeline = pipeline;
		this.params = params;
	}
	
	public void run() {
		for(Handler handler : pipeline) {
			handler.preprocess(params);
		}
	}
}
