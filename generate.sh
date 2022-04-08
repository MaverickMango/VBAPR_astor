#!/bin/bash
Base_Dir="/home/liu/Desktop/"
result="VBAPRResult"

cat "${Base_Dir}projs" | while read line
do
	proj=$(echo $line | cut -f 1 -d ":")
	bids=$(echo $line | cut -f 2 -d ":" | sed 's/,/ /g')
	echo "----------start $proj----------"
	cd ${Base_Dir}${result}
	mkdir $proj
	
	
	for i in $bids
	do	
		echo "----------$proj $i checkout----------"
		/home/liu/Desktop/defects4j/framework/bin/defects4j checkout -p $proj -v $i\b -w ./Defects4jProjs/$proj\_$i

		cd ./Defects4jProjs/$proj\_$i
		echo "----------$proj $i compile----------"
		/home/liu/Desktop/defects4j/framework/bin/defects4j compile

		cd ${Base_Dir}astor/
		echo "----------$proj $i astor run----------"
		if [[ "$proj" = "Math" ]]
		then
			java -cp target/astor-2.0.0-jar-with-dependencies.jar fr.inria.main.evolution.VBAPRMain -customengine fr.inria.astor.approaches.jgenprog.extension.VBAPR -srcjavafolder /src/main/java/ -srctestfolder /src/test/ -binjavafolder /target/classes/ -bintestfolder /target/test-classes/ -location ${BaseDir}${result}/Defects4jProjs/$proj\_$i/ -dependencies examples/$proj\_$i/lib -faultlocalization fr.inria.astor.core.faultlocalization.gzoltar.GZoltarFaultLocalizationWithGT -operatorspace fr.inria.astor.approaches.jgenprog.extension.VBAPRSpace -targetelementprocessor fr.inria.astor.core.manipulation.filters.SingleVariableFixSpaceProcessor -opselectionstrategy fr.inria.astor.core.solutionsearch.spaces.operators.GTBRepairOperatorSpace -ingredientstrategy fr.inria.astor.core.solutionsearch.spaces.ingredients.ingredientSearch.GTBSelectionIngredientSearchStrategy >> ConsoleOut_$proj\_$i 2>&1
		fi
		if [[ "$proj" = "Chart" ]]
		then
			java -cp target/astor-2.0.0-jar-with-dependencies.jar fr.inria.main.evolution.VBAPRMain -customengine fr.inria.astor.approaches.jgenprog.extension.VBAPR -srcjavafolder /source/ -srctestfolder /tests/  -binjavafolder /build/ -bintestfolder  /build-tests/ -location ${BaseDir}${result}/Defects4jProjs/$proj\_$i/ -dependencies ../${result}/Defects4jProjs/$proj\_$i/lib -faultlocalization fr.inria.astor.core.faultlocalization.gzoltar.GZoltarFaultLocalizationWithGT -operatorspace fr.inria.astor.approaches.jgenprog.extension.VBAPRSpace -targetelementprocessor fr.inria.astor.core.manipulation.filters.SingleVariableFixSpaceProcessor -opselectionstrategy fr.inria.astor.core.solutionsearch.spaces.operators.GTBRepairOperatorSpace -ingredientstrategy fr.inria.astor.core.solutionsearch.spaces.ingredients.ingredientSearch.GTBSelectionIngredientSearchStrategy >> ConsoleOut_$proj\_$i 2>&1
		fi
		mv ConsoleOut_$proj\_$i ${BaseDir}astor/output_astor/AstorMain-$proj\_$i
		echo "----------$proj $i astor done----------"
		cd ${Base_Dir}${result}/$proj
		mv ${BaseDir}astor/output_astor/AstorMain-$proj\_$i $proj\_$i
		cd $proj\_$i
		rm -rf bin
		cd src
		rm -rf default*
		dirs=$(ls -d variant*)
		for dirName in $dirs
		do
			cd $dirName
			rm -rf org
			cd ..
		done
		cd ${Base_Dir}${result}
	done
done

