# Hoshi: Japanese Morphological Adorner for TEI XML

Hoshi is a Japanese morphological adorner for TEI XML written in Java. The name *hoshi* means "star" in Japanese, which can be thought of in terms of the wildcard `*`, as in \*nix distros (the celestial body sense of the word works too :-). In the context of TEI XML, the application of parsers available online to deliver morphological information, which Hoshi then uses to adorn a TEI XML.

## Using Hoshi

The example below shows an input TEI XML, and a snippet of the output after adornment.

```xml
<TEI xmlns="http://www.tei-c.org/ns/1.0">
  <text>
    <body>
      <p>
        山<choice><sic>賊三人を</sic><corr>賊を三人</corr></choice>も撃ち倒し韋駄天。
      </p>
    </body>
  </text>
</TEI>
```

```xml
  <w token="山賊" type1="名詞" type2="一般" type3="*" type4="*" number="*" rule="*" root="山賊" spelled="サンゾク" spoken="サンゾク">
    山
  </w>
  <choice>
    <sic>
      賊三人を
    </sic>
    <corr>
      賊
      <w type1="助詞" type2="格助詞" type3="一般" type4="*" number="*" rule="*" root="を" spelled="ヲ" spoken="ヲ">
        を
      </w>
      <w type1="名詞" type2="数" type3="*" type4="*" number="*" rule="*" root="三" spelled="サン" spoken="サン">
        三
      </w>
      <w type1="名詞" type2="接尾" type3="助数詞" type4="*" number="*" rule="*" root="人" spelled="ニン" spoken="ニン">
        人
      </w>
    </corr>
  </choice>
```

The demo example is available in `data/`.

## Install

It is recommended to use the provided Makefile to compile and run Hoshi in the project directory. See the targets `compile` and `run`. The following options are available:

```
Japanese Morphological Adorner for TEI XML -- github.com/jerrybonnell/hoshi
usage: --analyze ANALYZER  --input FILE --output FILE [--model XML MODEL]
```

where

* `-analyze` can be any of the listed supported parsers
* `-input` is the input TEI XML to be adorned
* `-output` is the output TEI XML after adornment
* `-model` is an optional argument to specify the location of the custom schema needed to make the output a conformant custom TEI XML.

Additional requirements are needed for the following parsers..

### MeCab installation

Please install `mecab` using a package manager, e.g. Homebrew or apt-get. The incantation will look something like: `brew install mecab`. Try running the utility with sample input and make sure the results don't look like gibberish. If it does, you will need to build the IPADIC dictionary on your system.

The target `build-dict` is provided to build the dictionary on your system. It is also recommended to change its encoding to UTF-8 by using the `change-to-utf` target.

The `mecab-config` program may be needed for successful installation. See [this](https://github.com/mcho421/noj/issues/2) discussion for help or try installing MeCab using a package manager (e.g. Homebrew). Additionally, if  `mecab-dict-index` cannot be found while using the `change-to-utf` target, try changing the makefile variable `MECAB_DICT_INDEX` to point to its correct location.

### Kagome installation

To use this parser, please install the [Kagome](https://github.com/ikawaha/kagome) parser on your system.

### Sudachi installation

To use this parser, please install the [Sudachi](https://github.com/WorksApplications/Sudachi) parser on your system. The results generated will depend on the dictionary you installed. See here for the [options](https://github.com/WorksApplications/Sudachi#dictionaries).

## Supported parsers

Hoshi currently supports the following parsers:

* [Kuromoji](http://www.atilika.com/en/products/kuromoji.html)
* [MeCab](https://taku910.github.io/mecab/)
* [Kagome](https://github.com/ikawaha/kagome)
* [Sudachi](https://github.com/WorksApplications/Sudachi)

The standard IPADIC dictionary is used to build the model for each.

## Contact us

For more information regarding this project, please contact [Jerry Bonnell](mailto:j.bonnell@miami.edu) or [Mitsunori Ogihara](mailto:m.ogihara@miami.edu).
