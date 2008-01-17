cd neobio/lib
$SATIN_HOME/bin/satinc neobio.jar
cd -

cd lib
CLASSPATH=geneSequencing.jar $SATIN_HOME/bin/satinc \
  -satin geneSequencing.divideAndConquer.DivCon,geneSequencing.Dsearch,geneSequencing.sharedObjects.SharedData,geneSequencing.sharedObjects.DivConSO geneSequencing.jar
cd -
