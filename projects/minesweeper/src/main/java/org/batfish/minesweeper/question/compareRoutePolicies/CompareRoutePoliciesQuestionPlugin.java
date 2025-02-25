package org.batfish.minesweeper.question.compareRoutePolicies;

import com.google.auto.service.AutoService;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.QuestionPlugin;

/** QuestionPlugin for {@link CompareRoutePoliciesQuestion}. */
@AutoService(Plugin.class)
public final class CompareRoutePoliciesQuestionPlugin extends QuestionPlugin {
  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new CompareRoutePoliciesAnswerer((CompareRoutePoliciesQuestion) question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new CompareRoutePoliciesQuestion();
  }
}
