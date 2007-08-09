cd neobio/lib
$IBIS_HOME/bin/ibisc neobio.jar
cd -

cd lib
CLASSPATH=geneSequencing.jar $IBIS_HOME/bin/ibisc \
  -satin geneSequencing.divideAndConquer.DivCon,geneSequencing.Dsearch,geneSequencing.sharedObjects.SharedData,geneSequencing.sharedObjects.DivCon_so geneSequencing.jar
cd -
