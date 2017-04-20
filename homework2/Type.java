import ParseTree.Node;

import java.util.function.Consumer;

/**
 * Created by penguinni on 14.04.17.
 */
enum Type {
    Alpha(src -> Main.reBuilder.doAlpha()),
    Axiom(src -> Main.reBuilder.doAxiom(src[0])),
    AxiomScheme(src -> Main.reBuilder.doAxiom(src[0])),
    Hypothesis(src -> Main.reBuilder.doHypothesis(src[0])),
    MP(src -> Main.reBuilder.doMP(src[0], src[1])),
    RuleExists(src -> Main.reBuilder.doRuleExists(src[0], src[1])),
    RuleForAll(src -> Main.reBuilder.doRuleForAll(src[0], src[1]));

    Type(Consumer<Node[]> doRebuild) {
        this.doRebuild = doRebuild;
    }

    private static final ReBuilder reBuilder = null;
    private final Consumer<Node[]> doRebuild;

    void rebuild(Node[] src) {
        doRebuild.accept(src);
    }


}
