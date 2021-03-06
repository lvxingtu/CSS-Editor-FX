package xdean.css.editor.feature;

import java.util.stream.Stream;

import javax.inject.Inject;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.NavigationActions.SelectionPolicy;
import org.springframework.stereotype.Service;

import javafx.scene.control.IndexRange;
import xdean.css.editor.context.setting.EditActions;
import xdean.css.editor.control.CssEditor;

@Service
public class CommentFeature implements CssEditorFeature {

  private static final String LINE_COMMENT_PATTERN = "^\\s*/\\*.*\\*/\\s*$";

  @Inject
  EditActions actions;

  @Override
  public void bind(CssEditor editor) {
    editor.addEventHandler(actions.comment().getEventType(), e -> onAction((CssEditor) e.getSource()));
  }

  private void onAction(CssEditor editor) {
    selectLines(editor);
    String selectedText = editor.getSelectedText();
    IndexRange selection = editor.getSelection();
    editor.getUndoManager().preventMerge();
    editor.replaceSelection(CommentFeature.toggleComment(selectedText));
    editor.getUndoManager().preventMerge();
    editor.moveTo(selection.getStart(), SelectionPolicy.EXTEND);
  }

  private static void selectLines(CodeArea area) {
    IndexRange origin = area.getSelection();

    area.moveTo(origin.getStart());
    area.lineStart(SelectionPolicy.CLEAR);
    int start = area.getCaretPosition();

    area.moveTo(origin.getEnd());
    area.lineEnd(SelectionPolicy.CLEAR);
    int end = area.getCaretPosition();

    area.selectRange(start, end);
  }

  private static String toggleComment(String text) {
    String[] split = text.split("\\R");
    boolean allMatch = Stream.of(split).allMatch(s -> s.matches(LINE_COMMENT_PATTERN));
    if (allMatch) {
      return Stream.of(split).map(s -> clearComment(s))
          .reduce((s1, s2) -> String.join(System.lineSeparator(), s1, s2)).orElse(text);
    } else {
      return Stream.of(split).map(s -> addComment(s))
          .reduce((s1, s2) -> String.join(System.lineSeparator(), s1, s2)).orElse(text);
    }
  }

  private static String addComment(String line) {
    return "/* " + line + " */";
  }

  private static String clearComment(String line) {
    String trim = line.trim();
    String replace = trim.replaceAll("^/\\* ?", "").replaceAll(" ?\\*/$", "");
    return line.replace(trim, replace);
  }
}
