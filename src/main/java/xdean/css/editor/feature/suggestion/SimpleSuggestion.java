package xdean.css.editor.feature.suggestion;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import javafx.scene.control.IndexRange;
import xdean.css.editor.model.CssContext;

public class SimpleSuggestion implements CssSuggestionService {

  protected static final Set<Character> LEGAL_CHARS;

  static {
    Set<Character> set = new HashSet<>();
    for (char c = '0'; c <= '9'; c++) {
      set.add(c);
    }
    for (char c = 'a'; c <= 'z'; c++) {
      set.add(c);
      set.add((char) (c - 'a' + 'A'));
    }
    set.add('-');
    set.add('_');
    LEGAL_CHARS = Collections.unmodifiableSet(set);
  }

  @Inject
  CssSuggestionsFilter filter;

  @Override
  public Collection<String> getSuggestion(String text, int caretPos, CssContext context) {
    String prePart = text.substring(0, caretPos);
    IndexRange replaceRange = getReplaceRange(text, caretPos, context);
    char c = ' ';
    int index = replaceRange.getStart();
    while (index > 0) {
      index--;
      c = text.charAt(index);
      if (c != ' ' && c != '\t') {
        break;
      }
    }
    String keyWord = text.substring(replaceRange.getStart(), caretPos);

    boolean inBrace = prePart.lastIndexOf('{') > prePart.lastIndexOf('}');
    if (inBrace) {
      if (c == ':') {
        // XXX Can be smarter?
        // return filterSuggestion(CSSRepository.GLOBAL.getKeys(), keyWord);
        return Collections.emptyList();
      } else if (c == ' ' || c == '\t' || c == '\n' | c == '{') {
        return filter.filter(context.getKeys(), keyWord);
      } else {
        return Collections.emptyList();
      }
    } else {
      if (c == '.') {
        return filter.filter(context.getClasses(), keyWord);
      } else if (c == '#') {
        return filter.filter(context.getIds(), keyWord);
      } else if (c == ':') {
        return filter.filter(context.getStates(), keyWord);
      } else {
        return filter.filter(context.getJavaClasses(), keyWord);
      }
    }
  }

  @Override
  public IndexRange getReplaceRange(String text, int caretPos, CssContext context) {
    int start = caretPos - 1;
    int end = caretPos;
    while (start > -1 && LEGAL_CHARS.contains(text.charAt(start))) {
      start--;
    }

    while (end < text.length() && LEGAL_CHARS.contains(text.charAt(end))) {
      end++;
    }

    return new IndexRange(start + 1, end);
  }
}
