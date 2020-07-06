@echo off

set /p nomeSimples="Nome simples, sem espacos, do projeto (ex.: nutri, imobi...): "
echo Aguarde, isso pode demorar varios segundos...
for /R %%i in (*.java) do (
	sed -i "s/hw-framework/%nomeSimples%/g" %%i
)
for /R %%i in (*.cgp) do sed -i "s/hw-framework/%nomeSimples%/g" %%i
for /R %%i in (*.xml) do sed -i "s/hw-framework/%nomeSimples%/g" %%i
for /R %%i in (*.properties) do sed -i "s/hw-framework/%nomeSimples%/g" %%i
for /R %%i in (*db.bat) do sed -i "s/hw-framework/%nomeSimples%/g" %%i
for /R %%i in (*.lst) do sed -i "s/hw-framework/%nomeSimples%/g" %%i
for /R %%i in (*.project) do sed -i "s/hw-framework/%nomeSimples%/g" %%i
del /s sed*
del *.dll
del README.md
rd /S /Q ".git"
cd ..
ren hw-framework %nomeSimples%
echo Pronto, j� pode deletar o prepare.bat
