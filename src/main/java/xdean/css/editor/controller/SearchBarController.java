package xdean.css.editor.controller;

import static xdean.jex.util.lang.ExceptionUtil.uncatch;
import static xdean.jfxex.bean.ListenerUtil.on;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.controlsfx.control.textfield.TextFields;
import org.fxmisc.richtext.CodeArea;

import javafx.beans.property.Property;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import xdean.css.editor.config.Options;
import xdean.jex.extra.function.Func3;
import xdean.jex.util.string.StringUtil;
import xdean.jfx.spring.FxInitializable;
import xdean.jfx.spring.annotation.FxController;
import xdean.jfxex.bean.annotation.CheckNull;
import xdean.jfxex.bean.property.BooleanPropertyEX;
import xdean.jfxex.bean.property.ObjectPropertyEX;

@FxController(fxml = "/fxml/SearchBar.fxml")
public class SearchBarController implements FxInitializable {

  private @FXML HBox root;
  private @FXML HBox textContainer;
  private @FXML Button findButton;
  private @FXML CheckBox caseSensitive;
  private @FXML CheckBox regex;
  private @FXML CheckBox wrapSearch;

  private TextField findField;
  private final BooleanPropertyEX visible = new BooleanPropertyEX(this, "visible", false);
  private final ObjectPropertyEX<@CheckNull CodeArea> codeArea = new ObjectPropertyEX<>(this, "codeArea");

  @Override
  public void initAfterFxSpringReady() {
    findField = TextFields.createClearableTextField();
    textContainer.getChildren().add(findField);

    regex.selectedProperty().bindBidirectional(Options.findRegex.property());
    caseSensitive.selectedProperty().bindBidirectional(Options.findCaseSensitive.property());
    wrapSearch.selectedProperty().bindBidirectional(Options.findWrapText.property());
    visible.and(codeArea.isNotNull());

    root.visibleProperty().addListener(on(true, findField::requestFocus)
        .on(false, () -> uncatch(() -> codeArea.getValue().requestFocus())));
    root.visibleProperty().bind(visible);
    root.managedProperty().bind(root.visibleProperty());
    findField.setOnAction(e -> find());
  }

  @FXML
  public void find() {
    if (findFrom(codeArea.getValue().getCaretPosition()) == false && wrapSearch.isSelected()) {
      findFrom(0);
    }
  }

  private boolean findFrom(int offset) {
    CodeArea area = codeArea.getValue();
    String findText = findField.getText();
    if (regex.isSelected()) {
      return regexFind(area, findText, offset);
    } else {
      return simpleFind(area, findText, offset);
    }
  }

  private boolean simpleFind(CodeArea area, String text, int offset) {
    Func3<String, String, Integer, Integer> func = caseSensitive.isSelected() ? String::indexOf
        : StringUtil::indexOfIgnoreCase;
    int index = func.call(area.getText(), text, offset);
    if (index != -1) {
      area.selectRange(index, index + text.length());
      return true;
    }
    return false;
  }

  private boolean regexFind(CodeArea area, String regex, int offset) {
    if (caseSensitive.isSelected() && regex.startsWith("(?i)")) {
      regex = "?i" + regex;
    }
    Matcher matcher = Pattern.compile(regex).matcher(area.getText().substring(offset));
    if (matcher.find()) {
      area.selectRange(matcher.start() + offset, matcher.end() + offset);
      return true;
    }
    return false;
  }

  public void toggle() {
    visible.set(!visible.get());
  }

  public Property<CodeArea> codeAreaProperty() {
    return codeArea;
  }
}
