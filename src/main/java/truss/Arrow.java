package truss;

import javafx.scene.Group;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeLineCap;

public class Arrow extends Group{
    Line line;
    double arrowLength = 100;
    Joint j;
    double dir;
    Polygon arrowHead;

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

    
    // void recalc(){
    //     this.getPoints().clear();
    //     double dy = A.getCenterY()-B.getCenterY();
    //     double dx = A.getCenterX()-B.getCenterX();
    //     double theta = Math.PI/2+Math.atan2(dy, dx);
    //     dy = (0.5*A.getRadius())*Math.sin(theta);
    //     dx = (0.5*A.getRadius())*Math.cos(theta);
    //     this.getPoints().addAll(new Double[]{
    //         A.getCenterX()+dx, A.getCenterY()+dy,
    //         B.getCenterX()+dx, B.getCenterY()+dy,
    //         B.getCenterX()-dx, B.getCenterY()-dy,
    //         A.getCenterX()-dx, A.getCenterY()-dy
    //     });
    // }
}