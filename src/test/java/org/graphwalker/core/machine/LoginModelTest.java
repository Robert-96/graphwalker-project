package org.graphwalker.core.machine;

import org.graphwalker.core.condition.*;
import org.graphwalker.core.generator.AStarPath;
import org.graphwalker.core.generator.QuickRandomPath;
import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.generator.ShortestAllPaths;
import org.graphwalker.core.model.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This is a programatic implementaion of the model:
 * https://raw.githubusercontent.com/GraphWalker/graphwalker-cli/master/src/test/resources/graphml/shared_state/Login.graphml
 *
 * Created by krikar on 8/20/14.
 */
public class LoginModelTest {

    Vertex start = new Vertex().setName("Start").setStartVertex(true);
    Vertex v_Browse = new Vertex().setName("v_Browse").setSharedState("LOGGED_IN");
    Vertex v_ClientNotRunning = new Vertex().setName("v_ClientNotRunning").setSharedState("CLIENT_NOT_RUNNING");
    Vertex v_LoginPrompted = new Vertex().setName("v_LoginPrompted");

    Edge e_Close = new Edge().setName("e_Close").setSourceVertex(v_LoginPrompted).setTargetVertex(v_ClientNotRunning);
    Edge e_Exit = new Edge().setName("e_Exit").setSourceVertex(v_Browse).setTargetVertex(v_ClientNotRunning);
    Edge e_Init = new Edge().setName("e_Init").setSourceVertex(start).setTargetVertex(v_ClientNotRunning).addAction(new Action("validLogin=false")).addAction(new Action("rememberMe=false"));
    Edge e_InvalidCredentials = new Edge().setName("e_InvalidCredentials").setSourceVertex(v_LoginPrompted).setTargetVertex(v_LoginPrompted).addAction(new Action("validLogin=false"));
    Edge e_Logout = new Edge().setName("e_Logout").setSourceVertex(v_Browse).setTargetVertex(v_LoginPrompted);
    Edge e_StartClient_1 = new Edge().setName("e_StartClient").setSourceVertex(v_ClientNotRunning).setTargetVertex(v_LoginPrompted).setGuard(new Guard("!rememberMe||!validLogin"));
    Edge e_StartClient_2 = new Edge().setName("e_StartClient").setSourceVertex(v_ClientNotRunning).setTargetVertex(v_Browse).setGuard(new Guard("rememberMe&&validLogin"));
    Edge e_ToggleRememberMe = new Edge().setName("e_ToggleRememberMe").setSourceVertex(v_LoginPrompted).setTargetVertex(v_LoginPrompted).addAction(new Action("rememberMe=true"));
    Edge e_ValidPremiumCredentials = new Edge().setName("e_ValidPremiumCredentials").setSourceVertex(v_LoginPrompted).setTargetVertex(v_Browse).addAction(new Action("validLogin=true"));

    Model model = new Model().addEdge(e_Close).
        addEdge(e_Exit).
        addEdge(e_Init).
        addEdge(e_InvalidCredentials).
        addEdge(e_Logout).
        addEdge(e_StartClient_1).
        addEdge(e_StartClient_2).
        addEdge(e_ToggleRememberMe).
        addEdge(e_ValidPremiumCredentials);

    @Test
    public void ShortestAllPathEdgeCoverage() {
        ExecutionContext context = new ExecutionContext(model, new ShortestAllPaths(new EdgeCoverage(100)));
        Machine machine = new SimpleMachine(context);

        while (machine.hasNextStep()) {
            machine.getNextStep();
            System.out.println(context.getCurrentElement().getName());
        }
    }

    @Test
    public void ShortestAllPathEdgeAndVertexCoverage() {
        CombinedCondition combinedCondition = new CombinedCondition();
        combinedCondition.addStopCondition(new EdgeCoverage(100));
        combinedCondition.addStopCondition(new VertexCoverage(100));

        ExecutionContext context = new ExecutionContext(model, new ShortestAllPaths(combinedCondition));
        Machine machine = new SimpleMachine(context);

        while (machine.hasNextStep()) {
            machine.getNextStep();
            System.out.println(context.getCurrentElement().getName());
        }
    }

    @Test
    public void ShortestAllPathEdgeOrVertexCoverage() {
        AlternativeCondition alternativeCondition = new AlternativeCondition();
        alternativeCondition.addStopCondition(new EdgeCoverage(100));
        alternativeCondition.addStopCondition(new VertexCoverage(100));

        ExecutionContext context = new ExecutionContext(model, new ShortestAllPaths(alternativeCondition));
        Machine machine = new SimpleMachine(context);

        while (machine.hasNextStep()) {
            machine.getNextStep();
            System.out.println(context.getCurrentElement().getName());
        }
    }

    @Test
    public void AStarPathReachedEdgeExit() {
        ExecutionContext context = new ExecutionContext(model, new AStarPath(new ReachedEdge("e_Exit")));
        Machine machine = new SimpleMachine(context);

        while (machine.hasNextStep()) {
            machine.getNextStep();
            System.out.println(context.getCurrentElement().getName());
        }

        List<Element> expectedPath = Arrays.<Element>asList(
            e_Init.build(),
            v_ClientNotRunning.build(),
            e_StartClient_1.build(),
            v_LoginPrompted.build(),
            e_ValidPremiumCredentials.build(),
            v_Browse.build(),
            e_Exit.build());
        Collections.reverse(expectedPath);
        Assert.assertArrayEquals(expectedPath.toArray(), context.getProfiler().getPath().toArray());
    }

    @Test
    public void AStarPathReachedEdgeStartClient_2() {
        ExecutionContext context = new ExecutionContext(model, new AStarPath(new ReachedEdge("e_StartClient")));
        Machine machine = new SimpleMachine(context);

        while (machine.hasNextStep()) {
            machine.getNextStep();
            System.out.println(context.getCurrentElement().getName());
        }

        List<Element> expectedPath = Arrays.<Element>asList(
            e_Init.build(),
            v_ClientNotRunning.build(),
            e_StartClient_1.build(),
            v_LoginPrompted.build(),
            e_ValidPremiumCredentials.build(),
            v_Browse.build(),
            e_Exit.build());
        Collections.reverse(expectedPath);
        Assert.assertArrayEquals(expectedPath.toArray(), context.getProfiler().getPath().toArray());
    }

    @Test
    public void AStarPathReachedVertex() {
        ExecutionContext context = new ExecutionContext(model, new AStarPath(new ReachedVertex("v_Browse")));
        Machine machine = new SimpleMachine(context);

        while (machine.hasNextStep()) {
            machine.getNextStep();
            System.out.println(context.getCurrentElement().getName());
        }

        List<Element> expectedPath = Arrays.<Element>asList(
            e_Init.build(),
            v_ClientNotRunning.build(),
            e_StartClient_1.build(),
            v_LoginPrompted.build(),
            e_ValidPremiumCredentials.build(),
            v_Browse.build());
        Collections.reverse(expectedPath);
        Assert.assertArrayEquals(expectedPath.toArray(), context.getProfiler().getPath().toArray());
    }

    @Test
    public void QuickRandomPathEdgeCoverage() {
        ExecutionContext context = new ExecutionContext(model, new QuickRandomPath(new EdgeCoverage(100)));
        Machine machine = new SimpleMachine(context);

        while (machine.hasNextStep()) {
            machine.getNextStep();
            System.out.println(context.getCurrentElement().getName());
        }
    }

    @Test
    public void QuickRandomPathVertexCoverage() {
        ExecutionContext context = new ExecutionContext(model, new QuickRandomPath(new VertexCoverage(100)));
        Machine machine = new SimpleMachine(context);

        while (machine.hasNextStep()) {
            machine.getNextStep();
            System.out.println(context.getCurrentElement().getName());
        }
    }

    @Test
    public void QuickRandomPathEdgeAndVertexCoverage() {
        CombinedCondition combinedCondition = new CombinedCondition();
        combinedCondition.addStopCondition(new EdgeCoverage(100));
        combinedCondition.addStopCondition(new VertexCoverage(100));

        ExecutionContext context = new ExecutionContext(model, new QuickRandomPath(combinedCondition));
        Machine machine = new SimpleMachine(context);

        while (machine.hasNextStep()) {
            machine.getNextStep();
            System.out.println(context.getCurrentElement().getName());
        }
    }

    @Test
    public void QuickRandomPathEdgeOrVertexCoverage() {
        AlternativeCondition alternativeCondition = new AlternativeCondition();
        alternativeCondition.addStopCondition(new EdgeCoverage(100));
        alternativeCondition.addStopCondition(new VertexCoverage(100));

        ExecutionContext context = new ExecutionContext(model, new QuickRandomPath(alternativeCondition));
        Machine machine = new SimpleMachine(context);

        while (machine.hasNextStep()) {
            machine.getNextStep();
            System.out.println(context.getCurrentElement().getName());
        }
    }

    @Test
    public void RandomPathEdgeCoverage() {
        ExecutionContext context = new ExecutionContext(model, new RandomPath(new EdgeCoverage(100)));
        Machine machine = new SimpleMachine(context);

        while (machine.hasNextStep()) {
            machine.getNextStep();
            System.out.println(context.getCurrentElement().getName());
        }
    }

    @Test
    public void RandomPathVertexCoverage() {
        ExecutionContext context = new ExecutionContext(model, new RandomPath(new VertexCoverage(100)));
        Machine machine = new SimpleMachine(context);

        while (machine.hasNextStep()) {
            machine.getNextStep();
            System.out.println(context.getCurrentElement().getName());
        }
    }

    @Test
    public void RandomPathEdgeAndVertexCoverage() {
        CombinedCondition combinedCondition = new CombinedCondition();
        combinedCondition.addStopCondition(new EdgeCoverage(100));
        combinedCondition.addStopCondition(new VertexCoverage(100));

        ExecutionContext context = new ExecutionContext(model, new RandomPath(combinedCondition));
        Machine machine = new SimpleMachine(context);

        while (machine.hasNextStep()) {
            machine.getNextStep();
            System.out.println(context.getCurrentElement().getName());
        }
    }

    @Test
    public void RandomPathEdgeOrVertexCoverage() {
        AlternativeCondition alternativeCondition = new AlternativeCondition();
        alternativeCondition.addStopCondition(new EdgeCoverage(100));
        alternativeCondition.addStopCondition(new VertexCoverage(100));

        ExecutionContext context = new ExecutionContext(model, new RandomPath(alternativeCondition));
        Machine machine = new SimpleMachine(context);

        while (machine.hasNextStep()) {
            machine.getNextStep();
            System.out.println(context.getCurrentElement().getName());
        }
    }
}
