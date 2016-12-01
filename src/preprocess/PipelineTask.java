package preprocess;

import java.util.List;

public class PipelineTask {
	private List<Handler> pipeline;

	public PipelineTask(List<Handler> pipeline) {
		this.pipeline = pipeline;
	}
	
	public void runPipeline(Object task) {
		for(Handler handler : pipeline) {
			handler.preprocess(task);
		}
	}
}
