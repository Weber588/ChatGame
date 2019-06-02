package wbs.chatgame.game;

import java.util.HashSet;
import java.util.Set;

public class TriviaQuestion {

	private String question;
	private Set<String> answers = new HashSet<>();
	
	public TriviaQuestion(String question) {
		this.question = question;
	}
	
	public void addAnswer(String answer) {
		answers.add(answer);
	}
	
	public String getQuestion() {
		return question;
	}
	public Set<String> getAnswers() {
	    return answers;
	}
}
