package truss;

import static truss.App.*;

import java.util.ArrayList;
import java.util.List;

import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;   
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

public class InputHandling{
    static double mouseX = 0, mouseY = 0;
    static boolean pressedOnJoint = false;
    static Line line = new Line(0, 0, 0, 0);
    static Joint start = null, end = null;
    
    static EventHandler<MouseEvent> screenClick =  new EventHandler<MouseEvent>(){
        public void handle (MouseEvent me){
            for (Joint j: nodes){
                if (j.contains(me.getSceneX(), me.getSceneY())){
                    return;
                }
            }
            for (Beam b: beams){
                if (b.contains(me.getSceneX(), me.getSceneY())){
                    ContextMenu cm = null;
                    System.out.println("hello");
                    if (me.getButton() == MouseButton.PRIMARY){
                        MenuItem name = new MenuItem("Name: "+b.getName());
                        MenuItem force = new MenuItem("Force: "+b.getForce());
                        cm = new ContextMenu(name, force);
                    }
                    else if (me.getButton() == MouseButton.SECONDARY){
                        MenuItem delBeam = new MenuItem("Delete Beam");
                        delBeam.setOnAction(new EventHandler<ActionEvent>(){
                            public void handle(ActionEvent ae){
                                group.getChildren().remove(b);
                                b.getJointA().getBeams().remove(b);
                                b.getJointB().getBeams().remove(b);
                                beams.remove(b);
                            }
                        });
                        cm = new ContextMenu(delBeam);
                    }
                    cm.show(primaryStage, me.getSceneX(), me.getSceneY());
                    return;
                }
            }
            if (toggled && me.getX() < 0.2*pWidth){
                return;
            }
            if (me.getButton() == MouseButton.PRIMARY){
            }
            else if (me.getButton() == MouseButton.SECONDARY){
                MenuItem m = new MenuItem("test");
                MenuItem mm = new MenuItem ("this is a longer test");
                CheckMenuItem changeGrid = new CheckMenuItem("Show/Hide Grid");
                ImageView iv = new ImageView(new Image(getClass().getResourceAsStream("Show_Grid.png")));
                iv.setFitWidth(15);
                iv.setFitHeight(15);
                changeGrid.setGraphic(iv);
                changeGrid.setOnAction(new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent e) {
                        visibleGrid = !visibleGrid;
                        draw();
                    }
                });

                CheckMenuItem changeMinorGrid = new CheckMenuItem("Show/Hide Minor Gridlines");
                iv = new ImageView(new Image(getClass().getResourceAsStream("Show_Grid_Minor.png")));
                iv.setFitWidth(15);
                iv.setFitHeight(15);
                changeMinorGrid.setGraphic(iv);
                changeMinorGrid.setOnAction(new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent e) {
                        visibleMinorGrid = !visibleMinorGrid;
                        draw();
                    }
                });

                Menu showHideGrid = new Menu("Change Grid", null, changeGrid, changeMinorGrid);
                
                ContextMenu cm = new ContextMenu(m, mm, new SeparatorMenuItem(), showHideGrid);
                cm.show(primaryStage, me.getSceneX(), me.getSceneY());
            }
            else{   
            }
        }
    };

    static EventHandler<MouseEvent> jointClick = new EventHandler<MouseEvent>() {
        public void handle(MouseEvent me){
            ContextMenu cm = null;
            Joint clicked = null;

            if (me.getSource().getClass() == Text.class){
                Text t = (Text)me.getSource();
                clicked = nodeMap.get(t.getText());
            }
            else{
                clicked = (Joint)me.getSource();
            }

            if (me.getButton() == MouseButton.PRIMARY){
                MenuItem type = new MenuItem("Type: "+clicked.getTypes()[clicked.getType()]);
                MenuItem name = new MenuItem("Name: "+clicked.getName());
                cm = new ContextMenu(type, name);
            }
            else{
                MenuItem delete = new MenuItem("Delete");
                final Joint toDel = clicked;
                delete.setOnAction(new EventHandler<ActionEvent>(){
                    public void handle(ActionEvent ae){
                        for (Arrow a: toDel.getArrows()){
                            group.getChildren().remove(a);
                        }
                        for (Beam b: toDel.getBeams()){
                            Joint a = b.getJointA() == toDel ? b.getJointB() : b.getJointA();
                            a.getBeams().remove(b);
                            beams.remove(b);
                            group.getChildren().remove(b);
                        }
                        nodeMap.remove(toDel.getName());
                        nodes.remove(toDel);
                        group.getChildren().remove(toDel.getDisplayName());
                        group.getChildren().remove(toDel);
                    }
                });
                MenuItem delForces = new MenuItem("Remove Forces");
                delForces.setOnAction(new EventHandler<ActionEvent>(){
                    public void handle(ActionEvent ae){
                        group.getChildren().removeAll(toDel.getArrows());
                        toDel.getArrows().removeIf(p->true);
                        toDel.getForceDirs().removeIf(p->true);
                        toDel.getForceVals().removeIf(p->true);
                    }
                });
                MenuItem changeAng = new MenuItem("Change Orientation");
                if (clicked.getType() == NOTSUPP){
                    changeAng.setDisable(true);
                }
                final Joint toChange = clicked;
                changeAng.setOnAction(new EventHandler<ActionEvent>(){
                    public void handle(ActionEvent ae){
                        try{
                            TextInputDialog tid = new TextInputDialog();
                            tid.setTitle("Enter angle of support.");
                            tid.setHeaderText(null);
                            tid.showAndWait();
                            double newAng = Double.parseDouble(tid.getEditor().getText());
                            toChange.setAngle(newAng);
                        }catch(NumberFormatException nfe){
                            showError("You did not enter a valid number.");
                        }
                    }
                });
                cm = new ContextMenu(delete, delForces, changeAng);
            }
            cm.show(primaryStage, me.getSceneX(), me.getSceneY());
        }
    };

    static EventHandler<MouseEvent> screenDragged = new EventHandler<MouseEvent>() {
        public void handle(MouseEvent me){
            if (pressedOnJoint){
                return;
            }
            if (toggled && me.getX() < 0.2*pWidth){
                return;
            }
            pOffsetX += (me.getSceneX()-mouseX);
            pOffsetY += (me.getSceneY()-mouseY);

            List<Double> newPoints;
            for (Beam b: beams){
                newPoints = new ArrayList<Double>();
                for (int i = 0; i < b.getPoints().size(); i+=2){
                    newPoints.add(b.getPoints().get(i)+me.getSceneX()-mouseX);
                    newPoints.add(b.getPoints().get(i+1)+me.getSceneY()-mouseY);
                }
                b.getPoints().clear();
                b.getPoints().addAll(newPoints);
            }

            for (Joint j: nodes){
                j.setCenterX(j.getCenterX()+me.getSceneX()-mouseX);
                j.setCenterY(j.getCenterY()+me.getSceneY()-mouseY);
                j.getDisplayName().setX(j.getCenterX()-0.5*j.getDisplayName().getLayoutBounds().getWidth());
                j.getDisplayName().setY(j.getCenterY()+0.625*j.getRadius());
                for (Arrow a: j.getArrows()){
                    a.recalc();
                }
            }
            
            mouseX = me.getSceneX();
            mouseY = me.getSceneY();

            draw();
        }
    };

    static EventHandler<MouseEvent> jointDragged = new EventHandler<MouseEvent>() {
        public void handle (MouseEvent me){
            if (pressedOnJoint == false){
                pressedOnJoint = true;
                if (me.getSource().getClass() == Text.class){
                    Text t = (Text)me.getSource();
                    start = nodeMap.get(t.getText());
                }
                else{
                    start = (Joint)me.getSource();
                }
                line.setStartX(start.getCenterX());
                line.setStartY(start.getCenterY());
                group.getChildren().add(line);
                line.setStroke(Color.GRAY);
                line.setStrokeWidth(0.5*start.getRadius());
                line.setStrokeLineCap(StrokeLineCap.ROUND);
            }
            line.setEndX(me.getSceneX());
            line.setEndY(me.getSceneY());
        }
    };
    
    static EventHandler<MouseEvent> jointReleased = new EventHandler<MouseEvent>() {
        public void handle (MouseEvent me){
            pressedOnJoint = false;
            group.getChildren().remove(line);
            boolean check = true;
            for (Joint j: nodes){
                if (j.contains(me.getSceneX(), me.getSceneY())){
                    check = false;
                    end = j;
                    break;
                }
            }
            if (check){
                return;
            }
            try{
                Joint jointA = start;
                Joint jointB = end;
                start = null;
                end = null;
                if (jointA == jointB){
                    return;
                }
                for (Beam b: beams){
                    if (b.getJointA() == jointA && b.getJointB() == jointB){
                        return;
                    }
                    else if (b.getJointB() == jointA && b.getJointA() == jointB){
                        return;
                    }
                }
                new Beam(jointA, jointB);
            }catch(NullPointerException npe){
            }
        }
    };
    
    static EventHandler<MouseEvent> mouseMoved = new EventHandler<MouseEvent>() {
        public void handle(MouseEvent me){
            mouseX = me.getSceneX();
            mouseY = me.getSceneY();
        }
    };

    static EventHandler<ScrollEvent> mouseScrolled = new EventHandler<ScrollEvent>() {
        public void handle(ScrollEvent se){
            if (toggled && se.getX() < 0.2*pWidth){
                return;
            }
            double ini = 0, fin = 0;
            double delta = 0.05*se.getDeltaY()/Math.abs(se.getDeltaY());
            
            if (findStep(uWidth/22) < 1 && delta > 0){
                delta = 0;
            }
            if (uStep >= 100 && delta < 0){
                delta = 0;
            }

            ini = uStep*(delta*mouseX-pOffsetX-pWidth/2)/pStep;
            fin = uStep*(-delta*(pWidth-mouseX)-pOffsetX+pWidth/2)/pStep;
            pOffsetX = (ini*pWidth)/(ini-fin)-pWidth/2;
            uWidth = fin-ini;

            ini = uStep*(delta*mouseY-pOffsetY-pHeight/2)/pStep;
            fin = uStep*(-delta*(pHeight-mouseY)-pOffsetY+pHeight/2)/pStep;
            pOffsetY = (ini*pHeight)/(ini-fin)-pHeight/2;
            uHeight = fin-ini;

            for (Joint j: nodes){
                j.setCenterX(j.getUX()*pWidth/uWidth+pOffsetX+pWidth/2);
                j.setCenterY(-j.getUY()*pHeight/uHeight+pOffsetY+pHeight/2);
                j.setRadius(0.2*pStep/uStep);
                j.getDisplayName().setFont(new Font("DejaVu Sans Mono", 2*j.getRadius()));
                j.getDisplayName().setX(j.getCenterX()-0.5*j.getDisplayName().getLayoutBounds().getWidth());
                j.getDisplayName().setY(j.getCenterY()+0.625*j.getRadius());
                if (j.getRadius()<4){
                    j.setRadius(4);
                }
                for (Arrow a: j.getArrows()){
                    a.recalc();
                }
            }
            for (Beam b: beams){
                b.recalc();
            }
            draw();
        }
    };

    static EventHandler<ActionEvent> addNodeEventHandler = new EventHandler<ActionEvent>() {
        public void handle(ActionEvent ae){
            try{
                if (textfields[NODENAME].getText().equals("") ){
                    showError("Enter name for joint.");
                    return;
                }
                else if (nodeMap.containsKey(textfields[NODENAME].getText())){
                    showError("You cannot reuse a name.");
                    textfields[NODENAME].setText("");
                    textfields[NODENAME].requestFocus();
                    return;
                }
                else{
                    int type = 3;
                    if (rdButtons[ROLLER].isSelected()){
                        type = ROLLER;
                        rdButtons[ROLLER].setSelected(false);
                    }
                    else if (rdButtons[PIN].isSelected()){
                        type = PIN;
                        rdButtons[PIN].setSelected(false);
                    }
                    else if (rdButtons[FIXED].isSelected()){
                        type = FIXED;
                        rdButtons[FIXED].setSelected(false);
                    }
                    double x = Double.parseDouble(textfields[NODEX].getText());
                    double y = Double.parseDouble(textfields[NODEY].getText());
                    for (Joint j: nodes){
                        if (j.getUX() == x && j.getUY() == y){
                            showError("A joint or support already exists there.");
                            return;
                        }
                    }
                    Joint toAdd = new Joint(x, y, type, textfields[NODENAME].getText());
                    nodes.add(toAdd);
                    nodeMap.put(textfields[NODENAME].getText(), toAdd);
                    toAdd.addEventFilter(MouseEvent.MOUSE_CLICKED, jointClick);
                    toAdd.getDisplayName().addEventFilter(MouseEvent.MOUSE_CLICKED, jointClick);
                    toAdd.addEventFilter(MouseEvent.MOUSE_DRAGGED, jointDragged);
                    toAdd.getDisplayName().addEventFilter(MouseEvent.MOUSE_DRAGGED, jointDragged);
                    toAdd.addEventFilter(MouseEvent.MOUSE_RELEASED, jointReleased);
                    toAdd.getDisplayName().addEventFilter(MouseEvent.MOUSE_RELEASED, jointReleased);
    
                }
            }catch(NumberFormatException nfe){
                showError("Enter valid numbers for joint coordinates.");
            }
            textfields[NODENAME].setText("");
            textfields[NODEX].setText("");
            textfields[NODEY].setText("");
            textfields[NODENAME].requestFocus();
            draw();
        }
    };

    static EventHandler<ActionEvent> addBeamEventHandler = new EventHandler<ActionEvent>() {
        public void handle(ActionEvent ae){
            try{
                Joint jointA = nodeMap.get(textfields[BEAMNODEA].getText());
                Joint jointB = nodeMap.get(textfields[BEAMNODEB].getText());
                if (jointA == jointB){
                    showError("The two nodes must be unique.");
                    return;
                }
                for (Beam b: beams){
                    if (b.getJointA() == jointA && b.getJointB() == jointB){
                        showError("A beam between these two nodes already exists.");
                        return;
                    }
                    else if (b.getJointB() == jointA && b.getJointA() == jointB){
                        showError("A beam between these two nodes already exists.");
                        return;
                    }
                }
                new Beam(jointA, jointB);
            }catch(NullPointerException npe){
                showError("You entered an invalid name for one or both of the nodes.");
            }
            textfields[BEAMNODEA].setText("");
            textfields[BEAMNODEB].setText("");
            textfields[BEAMNODEA].requestFocus();
        }
    };

    static EventHandler<ActionEvent> addForceEventHandler = new EventHandler<ActionEvent>() {
        public void handle(ActionEvent ae){
            try{
                double text = 0;
                if ((text = Double.parseDouble(textfields[FORCEVAL].getText())) <= 0){
                    showError("Force value must be positive.");
                }
                Joint j = nodeMap.get(textfields[FORCENODE].getText());
                double angle = Double.parseDouble(textfields[FORCEDIR].getText());
                j.getForceVals().add(text);
                j.getForceDirs().add(angle);
                Arrow a = new Arrow(j, -angle);
                j.getArrows().add(a);
                group.getChildren().add(j.getArrows().get(j.getArrows().size()-1));
            }catch (NullPointerException npe){
                showError("You entered an invalid node.");
            }catch (NumberFormatException nfe){
                showError("You entered an invalid number.");
            }
            textfields[FORCENODE].setText("");
            textfields[FORCEDIR].setText("");
            textfields[FORCEVAL].setText("");
            textfields[FORCENODE].requestFocus();
        }
    };
    
    static EventHandler<ActionEvent> calculate = new EventHandler<ActionEvent>() {
        public void handle(ActionEvent ae){
            if (verify()){
                calculate();
            }
            else{
                System.out.println("error");
            }
        }
    };

    static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}