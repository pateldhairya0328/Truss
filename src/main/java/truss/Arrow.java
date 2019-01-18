package truss;

import java.text.DecimalFormat;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class Arrow extends Group{
    private Line line;
    private double arrowLength = 100;
    private Joint j;
    private double dir;
    private Polygon arrowHead;
    private Text force = new Text();

    public Arrow(Joint j, double dir, double val){
        this.j = j;
        this.dir = dir;
        arrowLength = App.pStep/App.uStep;
        double x = j.getCenterX();
        double y = j.getCenterY();
        line = new Line(x, y, x+0.8*arrowLength*Math.cos(Math.toRadians(dir)), y+0.8*arrowLength*Math.sin(Math.toRadians(dir)));
        line.setStrokeWidth(arrowLength/25);
        line.setStrokeLineCap(StrokeLineCap.ROUND);
        double ex = line.getEndX();
        double ey = line.getEndY();
        double sx = line.getStartX();
        double sy = line.getStartY();

        double dx = (sx - ex)*0.05;
        double dy = (sy - ey)*0.05;
        double ox = (sx - ex)*0.1;
        double oy = (sy - ey)*0.1;
        arrowHead = new Polygon(x+arrowLength*Math.cos(Math.toRadians(dir)), y+arrowLength*Math.sin(Math.toRadians(dir)), ex+dx-oy, ey+dy+ox, ex+dx+oy, ey+dy-ox);
        
        DecimalFormat df = new DecimalFormat("#.####");
        force.setText(df.format(val)+" kN");
        this.force.setX(x+0.8*arrowLength*Math.cos(Math.toRadians(dir)));
        this.force.setY(y+0.8*arrowLength*Math.sin(Math.toRadians(dir)));
        this.force.setStrokeWidth(0.2);
        this.force.setStroke(Color.WHITE);
        this.force.setFill(new Color(60/255, 60/255, 60/255, 1));
        this.force.setFont(new Font("DejaVu Sans Mono", (int)(arrowLength/5)));

        this.getChildren().addAll(line, arrowHead, force);
    }

    public Arrow(Joint j, double dir, double val, boolean reactionForce){
        this(j, dir, val);
        if (reactionForce){
            line.setStroke(Color.PURPLE);
            line.setFill(Color.PURPLE);
            arrowHead.setFill(Color.PURPLE);
            arrowHead.setStroke(Color.PURPLE);
        }
    }

    void recalc(){
        arrowLength = App.pStep/App.uStep;
        double x = j.getCenterX();
        double y = j.getCenterY();
        line.setStartX(x);
        line.setStartY(y);
        line.setEndX(x+0.8*arrowLength*Math.cos(Math.toRadians(dir)));
        line.setEndY(y+0.8*arrowLength*Math.sin(Math.toRadians(dir)));
        line.setStrokeWidth(arrowLength/25);
        double ex = line.getEndX();
        double ey = line.getEndY();
        double sx = line.getStartX();
        double sy = line.getStartY();

        double dx = (sx - ex)*0.05;
        double dy = (sy - ey)*0.05;
        double ox = (sx - ex)*0.1;
        double oy = (sy - ey)*0.1;
        arrowHead.getPoints().clear();
        arrowHead.getPoints().addAll(x+arrowLength*Math.cos(Math.toRadians(dir)), y+arrowLength*Math.sin(Math.toRadians(dir)), ex+dx-oy, ey+dy+ox, ex+dx+oy, ey+dy-ox);
        
        force.setX(x+0.8*arrowLength*Math.cos(Math.toRadians(dir)));
        force.setY(y+0.8*arrowLength*Math.sin(Math.toRadians(dir)));
        force.setFont(new Font("DejaVu Sans Mono", (int)(arrowLength/5)));
    }

    public Line getLine(){
        return line;
    }

    public void setLine(Line line){
        this.line = line;
    }

    public double getArrowLength(){
        return arrowLength;
    }

    public Joint getJoint(){
        return j;
    }

    public void setJoint(Joint j){
        this.j = j;
    }

    public double getDir(){
        return dir;
    }

    public void setDir(double dir){
        this.dir = dir;
    }

    public Polygon getArrowhead(){
        return arrowHead;
    }

    public void setForce(double val){
        this.force.setText(val+" kN");
    }

    public String getForceText(){
        return force.getText();
    }

    public Text getForce(){
        return force;
    }
}