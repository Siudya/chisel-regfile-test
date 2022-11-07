rtl:
	mill RegFile.test.runMain GenRTL -td build --full-stacktrace -X sverilog --emission-options disableRegisterRandomization

test:
	mill RegFile.test.test

idea:
	mill -i mill.scalalib.GenIdea/idea

bsp:
	mill -i mill.bsp.BSP/install

clean:
	rm -rf ./build

.PHONY: clean idea bsp test