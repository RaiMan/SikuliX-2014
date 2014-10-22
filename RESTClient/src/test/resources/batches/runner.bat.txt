@echo off

pushd %~dp0

START CMD /C CALL sampleBatch.bat %*

popd