
package model;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YandexSpellerAnswer {

    @SerializedName("code")
    @Expose
    private Integer code;

    @SerializedName("pos")
    @Expose
    private Integer pos;

    @SerializedName("row")
    @Expose
    private Integer row;

    @SerializedName("col")
    @Expose
    private Integer col;

    @SerializedName("len")
    @Expose
    private Integer len;

    @SerializedName("word")
    @Expose
    private String word;

    @SerializedName("s")
    @Expose
    private List<String> s = new ArrayList<String>();

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(YandexSpellerAnswer.class.getName()).append('@')
                .append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("code").append('=').append(((this.code == null) ? "<null>" : this.code));
        sb.append(',').append("pos").append('=').append(((this.pos == null) ? "<null>" : this.pos));
        sb.append(',').append("row").append('=').append(((this.row == null) ? "<null>" : this.row));
        sb.append(',').append("col").append('=').append(((this.col == null) ? "<null>" : this.col));
        sb.append(',').append("len").append('=').append(((this.len == null) ? "<null>" : this.len));
        sb.append(',').append("word").append('=').append(((this.word == null) ? "<null>" : this.word));
        sb.append(',').append("s").append('=').append(((this.s == null) ? "<null>" : this.s));
        sb.append(',');
        if (sb.charAt((sb.length() - 1)) == ',') {
            sb.setCharAt((sb.length() - 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result * 31) + ((this.col == null) ? 0 : this.col.hashCode()));
        result = ((result * 31) + ((this.code == null) ? 0 : this.code.hashCode()));
        result = ((result * 31) + ((this.s == null) ? 0 : this.s.hashCode()));
        result = ((result * 31) + ((this.len == null) ? 0 : this.len.hashCode()));
        result = ((result * 31) + ((this.pos == null) ? 0 : this.pos.hashCode()));
        result = ((result * 31) + ((this.row == null) ? 0 : this.row.hashCode()));
        result = ((result * 31) + ((this.word == null) ? 0 : this.word.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof YandexSpellerAnswer) == false) {
            return false;
        }
        YandexSpellerAnswer rhs = ((YandexSpellerAnswer) other);
        return ((((((((this.col == rhs.col) || ((this.col != null) && this.col.equals(rhs.col)))
                && ((this.code == rhs.code) || ((this.code != null) && this.code.equals(rhs.code))))
                && ((this.s == rhs.s) || ((this.s != null) && this.s.equals(rhs.s))))
                && ((this.len == rhs.len) || ((this.len != null) && this.len.equals(rhs.len))))
                && ((this.pos == rhs.pos) || ((this.pos != null) && this.pos.equals(rhs.pos))))
                && ((this.row == rhs.row) || ((this.row != null) && this.row.equals(rhs.row))))
                && ((this.word == rhs.word) || ((this.word != null) && this.word.equals(rhs.word))));
    }

}
