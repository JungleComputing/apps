package geneSequencing;

import java.util.ArrayList;

public final class Sequence implements java.io.Serializable {
    private String sequenceName;

    private String sequenceBody;

    private int score;

    private String alignment;

    public Sequence() {
        sequenceName = "";
        sequenceBody = "";
        score = 0;
        alignment = "not calculated";
    }

    public Sequence(String name, ArrayList<String> body) {
        sequenceName = name;
        sequenceBody = createBody(body);
        score = 0;
        alignment = "not calculated";
    }

    public Sequence(Sequence seq) {
        sequenceName = seq.getSequenceName();
        sequenceBody = seq.getSequenceBody();
        score = seq.getSequenceScore();
        alignment = seq.getSequenceAlignment();
    }

    public String getSequenceName() {
        return sequenceName;
    }

    public String getSequenceBody() {
        return sequenceBody;
    }

    public int getSequenceScore() {
        return score;
    }

    public String getSequenceAlignment() {
        return alignment;
    }

    public void setSequenceScore(int score) {
        this.score = score;
    }

    public void setSequenceAlignment(String alignment) {
        this.alignment = alignment;
    }

    private String createBody(ArrayList<String> parts) {
        StringBuffer body = new StringBuffer();
        for (int i = 0; i < parts.size(); i++) {
            body.append(parts.get(i));
        }

        return body.toString();
    }
}
