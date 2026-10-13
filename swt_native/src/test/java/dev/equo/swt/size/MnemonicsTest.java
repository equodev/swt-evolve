package dev.equo.swt.size;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** The cases match flutter-lib/test/mnemonic_text_test.dart: Dart draws by the rule Java measures by. */
class MnemonicsTest {

    @Test
    void text_without_a_marker_is_returned_as_it_is() {
        assertThat(Mnemonics.display("Save")).isEqualTo("Save");
        assertThat(Mnemonics.display("")).isEmpty();
        assertThat(Mnemonics.display(null)).isNull();
    }

    @Test
    void a_single_ampersand_marks_the_next_character_and_is_not_drawn() {
        assertThat(Mnemonics.display("&Save")).isEqualTo("Save");
        assertThat(Mnemonics.display("Sa&ve")).isEqualTo("Save");
        assertThat(Mnemonics.display("Save&")).isEqualTo("Save");
    }

    @Test
    void every_single_ampersand_is_dropped_not_only_the_first() {
        assertThat(Mnemonics.display("&File &Edit")).isEqualTo("File Edit");
        assertThat(Mnemonics.display("&")).isEmpty();
    }

    @Test
    void the_string_itself_comes_back_when_it_holds_no_marker() {
        String text = "Save";
        assertThat(Mnemonics.display(text)).isSameAs(text);
    }

    @Test
    void a_doubled_ampersand_is_drawn_once() {
        assertThat(Mnemonics.display("a && b")).isEqualTo("a & b");
        assertThat(Mnemonics.display("&&Save")).isEqualTo("&Save");
    }

    @Test
    void ampersands_pair_up_from_the_left() {
        assertThat(Mnemonics.display("&&&")).isEqualTo("&");
        assertThat(Mnemonics.display("&&&&")).isEqualTo("&&");
        assertThat(Mnemonics.display("&&&Save")).isEqualTo("&Save");
    }
}
