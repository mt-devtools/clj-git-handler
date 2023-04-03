
# Updating submodules by using the current HEAD

A submodule .git könyvtárának HEAD fájljában az aktuális branch kiolvasható
így nem kellene megadni az updater függvénynek. És elkerülhetőek lennének
azok az esetek, amikor véletlenül release branch-on hagyom a Fork-ot, ott dolgozok
és az updater push-olja a változásokat a development branch-ra érdekes
kanyarokat okozva ezzel a git history-ban
