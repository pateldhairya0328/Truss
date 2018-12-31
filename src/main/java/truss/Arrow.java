package truss;

import javafx.scene.Group;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeLineCap;

public class Arrow extends Group{
    private Line line;
    private double arrowLength = 100;
    private Joint j;
    private double dir;
    private Polygon arrowHead;

    public Arrow(Joint j, double dir){
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
        this.getChildren().addAll(line, arrowHead);
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
}