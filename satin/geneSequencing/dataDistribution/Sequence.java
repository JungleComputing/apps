//package dsearchDC;

import java.util.Vector;

public class Sequence implements java.io.Serializable
{
    private String sequenceName;
    private Vector sequenceBody;
    private int score;
    private String alignment;

    public Sequence()
    {
        sequenceName = new String();
        sequenceBody = new Vector();
        score        = 0;
        alignment    = "not calculated";
    }

    public Sequence(String name, Vector body)
    {
        sequenceName = name;
        sequenceBody = body;
        score        = 0;
        alignment    = "not calculated";
    }

    public Sequence(Sequence seq)
    {
        sequenceName = new String(seq.getSequenceName());
        sequenceBody = new Vector(seq.getSequenceBody());
        score        = new Integer(seq.getSequenceScore());
        alignment    = new String(seq.getSequenceAlignment());
    }

    public String getSequenceName()
    {
        return sequenceName;
    }

    public Vector getSequenceBody()
    {
        return sequenceBody;
    }

    public int getSequenceScore()
    {
        return score;
    }

    public String getSequenceAlignment()
    {
        return alignment;
    }
    public void setSequenceName(String name)
    {
        sequenceName = name;
    }

    public void setSequenceBody(Vector body)
    {
        sequenceBody = body;
    }

    public void setSequenceScore(int score)
    {
        this.score = score;
    }

    public void setSequenceAlignment(String alignment)
    {
        this.alignment = new String(alignment);
    }

    public String createSequenceBody()
    {
        String body = new String();
        for(int i = 0; i < sequenceBody.size(); i++)
        {
            body = body.concat((String)sequenceBody.get(i));
        }

        return body;
    }
}
                        
