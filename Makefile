PROJNAME:=kineticform
PROJVERSION:=0.0.1

LIBDIR:=./lib
SCRIPTDIR:=./scripts
PACKAGEDIR:=./out/package/

JAR:=./out/artifacts/kineticform_jar/kineticform.jar
SCRIPTS:=$(shell find $(SCRIPTDIR) -type f)

PACKAGE:=$(PACKAGEDIR)/$(PROJNAME)-$(PROJVERSION).zip

all:	$(PACKAGEDIR) $(PACKAGE)

deploy:	all
	scp $(PACKAGE) 'pi@10.0.0.3:~/kineticform/'

$(PACKAGE):	$(JAR) $(SCRIPTS) $(LIBDIR)
	zip -r $@ $^

$(PACKAGEDIR):
	mkdir -p $@

clean:
	rm -rf $(PACKAGEDIR)

.PHONY:	clean
