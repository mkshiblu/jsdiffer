package io.jsrminer.uml.mapping.replacement;

import io.jsrminer.sourcetree.ObjectCreation;
import io.jsrminer.sourcetree.SingleStatement;
import io.jsrminer.sourcetree.VariableDeclaration;

import java.util.List;

// TODO rename class
public class ReplacementHeuristic {

    public boolean isObjectCreationReplacedWithArrayDeclaration(SingleStatement statement1, SingleStatement statement2
            , ReplacementInfo replacementInfo) {
        final ObjectCreation creationCoveringTheEntireStatement1 = InvocationCoverage.INSTANCE.creationCoveringEntireFragment(statement1);
        final ObjectCreation creationCoveringTheEntireStatement2 = InvocationCoverage.INSTANCE.creationCoveringEntireFragment(statement2);
        final List<VariableDeclaration> variableDeclarations1 = statement1.getVariableDeclarations();
        final List<VariableDeclaration> variableDeclarations2 = statement2.getVariableDeclarations();

        //check if array creation is replaced with data structure creation
        if (creationCoveringTheEntireStatement1 != null && creationCoveringTheEntireStatement2 != null &&
                variableDeclarations1.size() == 1 && variableDeclarations2.size() == 1) {

            VariableDeclaration v1 = variableDeclarations1.get(0);
            VariableDeclaration v2 = variableDeclarations2.get(0);

            String initializer1 = v1.getInitializer() != null ? v1.getInitializer().getText() : null;
            String initializer2 = v2.getInitializer() != null ? v2.getInitializer().getText() : null;

//            Replacement r;

//            boolean isArrayCreationReplacedWithObjectCreation = (creationCoveringTheEntireStatement1.isArray()
//                    && !creationCoveringTheEntireStatement2.isArray());
//
//            boolean isObjectCreationReplacedWithArrayCreation = (creationCoveringTheEntireStatement2.isArray()
//                    && !creationCoveringTheEntireStatement1.isArray());
//
//            boolean sameArguments = false;
//
//            if (initializer1 != null && initializer2 != null) {
//                String arrayElements1 = initializer1
//                        .substring(initializer1.indexOf("[") + 1
//                                , initializer1.lastIndexOf("]"));
//                String objectCreationArguments1 = initializer2.substring(initializer2.indexOf("(") + 1,
//                        initializer2.lastIndexOf("]"));
//
//                sameArguments = arrayElements1.equals(objectCreationArguments1);
//            }

            boolean creation2IsArrayConstructor = "Array".equals(creationCoveringTheEntireStatement2.getFunctionName());
            boolean creation1IsArrayConstructor = "Array".equals(creationCoveringTheEntireStatement1.getFunctionName());

//            boolean creation1IsEmptyArray = "[]".equals(creationCoveringTheEntireStatement1.getText());
            //          boolean creation2IsEmptyArray = "[]".equals(creationCoveringTheEntireStatement2.getText());

            // Check if replaced With built int types;
            boolean arrayCreationReplacedWithArrayConstructor = creationCoveringTheEntireStatement1.isArray()
                    && creation2IsArrayConstructor;
            boolean arrayConstructorReplacedWithArrayCreation = creationCoveringTheEntireStatement2.isArray()
                    && creation1IsArrayConstructor;

            if (arrayConstructorReplacedWithArrayCreation || arrayCreationReplacedWithArrayConstructor) {
                Replacement replacement = new ObjectCreationReplacement(initializer1, initializer2,
                        creationCoveringTheEntireStatement1, creationCoveringTheEntireStatement2, Replacement.ReplacementType.ARRAY_CONSTRUCTOR_REPLACED_WITH_ARRAY_CREATION);
                replacementInfo.addReplacement(replacement);
                return true;
            }
        }
        return false;
    }
}
