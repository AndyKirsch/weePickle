package com.rallyhealth.weepack.v0
import java.io.ByteArrayOutputStream

import com.rallyhealth.weejson.v0.WeeJson
import com.rallyhealth.weepickle.v0.core.Util
import utest._
object MsgPackTests extends TestSuite{
  val tests = Tests{
    test("hello"){
      for{
        (k, v) <- unitCases.obj
        if k != "50.timestamp.yaml"

        testCase <- v.arr
        packed0 <- testCase("msgpack").arr
      }{
        val (tag, expectedJson) = testCase.obj.find{_._1 != "msgpack"}.get
        val packedStr = packed0.str
        println(k + " " + tag + " " + expectedJson + " " + packedStr)
        val packed = Util.stringToBytes(packedStr)

        val jsonified0 = WeePack.transform(packed, com.rallyhealth.weejson.v0.Value)

        val jsonified = tag match{
          case "binary" => com.rallyhealth.weejson.v0.Str(Util.bytesToString(jsonified0.arr.map(_.num.toByte).toArray))
          case "ext" => com.rallyhealth.weejson.v0.Arr(jsonified0(0), com.rallyhealth.weejson.v0.Str(Util.bytesToString(jsonified0(1).arr.map(_.num.toByte).toArray)))
          case _ => jsonified0
        }
        assert(jsonified == expectedJson)

        val msg = WeePack.read(packed)

        val rewrittenBytes = WeePack.write(msg)
        val rewritten = Util.bytesToString(rewrittenBytes)
        val possibilities = testCase("msgpack").arr.map(_.str)

        assert(possibilities.contains(rewritten))
      }
    }
  }

  // Taken from:
  // https://github.com/kawanet/msgpack-test-suite/tree/e04f6edeaae589c768d6b70fcce80aa786b7800e
  val unitCases = WeeJson.read("""
    {
      "10.nil.yaml": [
        {
          "nil": null,
          "msgpack": [
            "c0"
          ]
        }
      ],
      "11.bool.yaml": [
        {
          "bool": false,
          "msgpack": [
            "c2"
          ]
        },
        {
          "bool": true,
          "msgpack": [
            "c3"
          ]
        }
      ],
      "12.binary.yaml": [
        {
          "binary": "",
          "msgpack": [
            "c4-00",
            "c5-00-00",
            "c6-00-00-00-00"
          ]
        },
        {
          "binary": "01",
          "msgpack": [
            "c4-01-01",
            "c5-00-01-01",
            "c6-00-00-00-01-01"
          ]
        },
        {
          "binary": "00-ff",
          "msgpack": [
            "c4-02-00-ff",
            "c5-00-02-00-ff",
            "c6-00-00-00-02-00-ff"
          ]
        }
      ],
      "20.number-positive.yaml": [
        {
          "number": 0,
          "msgpack": [
            "00",
            "cc-00",
            "cd-00-00",
            "ce-00-00-00-00",
            "cf-00-00-00-00-00-00-00-00",
            "d0-00",
            "d1-00-00",
            "d2-00-00-00-00",
            "d3-00-00-00-00-00-00-00-00",
            "ca-00-00-00-00",
            "cb-00-00-00-00-00-00-00-00"
          ]
        },
        {
          "number": 1,
          "msgpack": [
            "01",
            "cc-01",
            "cd-00-01",
            "ce-00-00-00-01",
            "cf-00-00-00-00-00-00-00-01",
            "d0-01",
            "d1-00-01",
            "d2-00-00-00-01",
            "d3-00-00-00-00-00-00-00-01",
            "ca-3f-80-00-00",
            "cb-3f-f0-00-00-00-00-00-00"
          ]
        },
        {
          "number": 127,
          "msgpack": [
            "7f",
            "cc-7f",
            "cd-00-7f",
            "ce-00-00-00-7f",
            "cf-00-00-00-00-00-00-00-7f",
            "d0-7f",
            "d1-00-7f",
            "d2-00-00-00-7f",
            "d3-00-00-00-00-00-00-00-7f"
          ]
        },
        {
          "number": 128,
          "msgpack": [
            "cc-80",
            "cd-00-80",
            "ce-00-00-00-80",
            "cf-00-00-00-00-00-00-00-80",
            "d1-00-80",
            "d2-00-00-00-80",
            "d3-00-00-00-00-00-00-00-80"
          ]
        },
        {
          "number": 255,
          "msgpack": [
            "cc-ff",
            "cd-00-ff",
            "ce-00-00-00-ff",
            "cf-00-00-00-00-00-00-00-ff",
            "d1-00-ff",
            "d2-00-00-00-ff",
            "d3-00-00-00-00-00-00-00-ff"
          ]
        },
        {
          "number": 256,
          "msgpack": [
            "cd-01-00",
            "ce-00-00-01-00",
            "cf-00-00-00-00-00-00-01-00",
            "d1-01-00",
            "d2-00-00-01-00",
            "d3-00-00-00-00-00-00-01-00"
          ]
        },
        {
          "number": 65535,
          "msgpack": [
            "cd-ff-ff",
            "ce-00-00-ff-ff",
            "cf-00-00-00-00-00-00-ff-ff",
            "d2-00-00-ff-ff",
            "d3-00-00-00-00-00-00-ff-ff"
          ]
        },
        {
          "number": 65536,
          "msgpack": [
            "ce-00-01-00-00",
            "cf-00-00-00-00-00-01-00-00",
            "d2-00-01-00-00",
            "d3-00-00-00-00-00-01-00-00"
          ]
        },
        {
          "number": 2147483647,
          "msgpack": [
            "ce-7f-ff-ff-ff",
            "cf-00-00-00-00-7f-ff-ff-ff",
            "d2-7f-ff-ff-ff",
            "d3-00-00-00-00-7f-ff-ff-ff"
          ]
        },
        {
          "number": 2147483648,
          "msgpack": [
            "ce-80-00-00-00",
            "cf-00-00-00-00-80-00-00-00",
            "d3-00-00-00-00-80-00-00-00",
            "ca-4f-00-00-00",
            "cb-41-e0-00-00-00-00-00-00"
          ]
        },
        {
          "number": 4294967295,
          "msgpack": [
            "ce-ff-ff-ff-ff",
            "cf-00-00-00-00-ff-ff-ff-ff",
            "d3-00-00-00-00-ff-ff-ff-ff",
            "cb-41-ef-ff-ff-ff-e0-00-00"
          ]
        }
      ],
      "21.number-negative.yaml": [
        {
          "number": -1,
          "msgpack": [
            "ff",
            "d0-ff",
            "d1-ff-ff",
            "d2-ff-ff-ff-ff",
            "d3-ff-ff-ff-ff-ff-ff-ff-ff",
            "ca-bf-80-00-00",
            "cb-bf-f0-00-00-00-00-00-00"
          ]
        },
        {
          "number": -32,
          "msgpack": [
            "e0",
            "d0-e0",
            "d1-ff-e0",
            "d2-ff-ff-ff-e0",
            "d3-ff-ff-ff-ff-ff-ff-ff-e0",
            "ca-c2-00-00-00",
            "cb-c0-40-00-00-00-00-00-00"
          ]
        },
        {
          "number": -33,
          "msgpack": [
            "d0-df",
            "d1-ff-df",
            "d2-ff-ff-ff-df",
            "d3-ff-ff-ff-ff-ff-ff-ff-df"
          ]
        },
        {
          "number": -128,
          "msgpack": [
            "d0-80",
            "d1-ff-80",
            "d2-ff-ff-ff-80",
            "d3-ff-ff-ff-ff-ff-ff-ff-80"
          ]
        },
        {
          "number": -256,
          "msgpack": [
            "d1-ff-00",
            "d2-ff-ff-ff-00",
            "d3-ff-ff-ff-ff-ff-ff-ff-00"
          ]
        },
        {
          "number": -32768,
          "msgpack": [
            "d1-80-00",
            "d2-ff-ff-80-00",
            "d3-ff-ff-ff-ff-ff-ff-80-00"
          ]
        },
        {
          "number": -65536,
          "msgpack": [
            "d2-ff-ff-00-00",
            "d3-ff-ff-ff-ff-ff-ff-00-00"
          ]
        },
        {
          "number": -2147483648,
          "msgpack": [
            "d2-80-00-00-00",
            "d3-ff-ff-ff-ff-80-00-00-00",
            "cb-c1-e0-00-00-00-00-00-00"
          ]
        }
      ],
      "22.number-float.yaml": [
        {
          "number": 0.5,
          "msgpack": [
            "ca-3f-00-00-00",
            "cb-3f-e0-00-00-00-00-00-00"
          ]
        },
        {
          "number": -0.5,
          "msgpack": [
            "ca-bf-00-00-00",
            "cb-bf-e0-00-00-00-00-00-00"
          ]
        }
      ],
      "23.number-bignum.yaml": [
        {
          "number": 4294967296,
          "bignum": "4294967296",
          "msgpack": [
            "cf-00-00-00-01-00-00-00-00",
            "d3-00-00-00-01-00-00-00-00",
            "ca-4f-80-00-00",
            "cb-41-f0-00-00-00-00-00-00"
          ]
        },
        {
          "number": -4294967296,
          "bignum": "-4294967296",
          "msgpack": [
            "d3-ff-ff-ff-ff-00-00-00-00",
            "cb-c1-f0-00-00-00-00-00-00"
          ]
        },
        {
          "number": 281474976710656,
          "bignum": "281474976710656",
          "msgpack": [
            "cf-00-01-00-00-00-00-00-00",
            "d3-00-01-00-00-00-00-00-00",
            "ca-57-80-00-00",
            "cb-42-f0-00-00-00-00-00-00"
          ]
        },
        {
          "number": -281474976710656,
          "bignum": "-281474976710656",
          "msgpack": [
            "d3-ff-ff-00-00-00-00-00-00",
            "ca-d7-80-00-00",
            "cb-c2-f0-00-00-00-00-00-00"
          ]
        },
        {
          "bignum": "9223372036854775807",
          "msgpack": [
            "d3-7f-ff-ff-ff-ff-ff-ff-ff",
            "cf-7f-ff-ff-ff-ff-ff-ff-ff"
          ]
        },
        {
          "bignum": "-9223372036854775807",
          "msgpack": [
            "d3-80-00-00-00-00-00-00-01"
          ]
        },
        {
          "bignum": "9223372036854775808",
          "msgpack": [
            "cf-80-00-00-00-00-00-00-00"
          ]
        },
        {
          "bignum": "-9223372036854775808",
          "msgpack": [
            "d3-80-00-00-00-00-00-00-00"
          ]
        },
        {
          "bignum": "18446744073709551615",
          "msgpack": [
            "cf-ff-ff-ff-ff-ff-ff-ff-ff"
          ]
        }
      ],
      "30.string-ascii.yaml": [
        {
          "string": "",
          "msgpack": [
            "a0",
            "d9-00",
            "da-00-00",
            "db-00-00-00-00"
          ]
        },
        {
          "string": "a",
          "msgpack": [
            "a1-61",
            "d9-01-61",
            "da-00-01-61",
            "db-00-00-00-01-61"
          ]
        },
        {
          "string": "1234567890123456789012345678901",
          "msgpack": [
            "bf-31-32-33-34-35-36-37-38-39-30-31-32-33-34-35-36-37-38-39-30-31-32-33-34-35-36-37-38-39-30-31",
            "d9-1f-31-32-33-34-35-36-37-38-39-30-31-32-33-34-35-36-37-38-39-30-31-32-33-34-35-36-37-38-39-30-31",
            "da-00-1f-31-32-33-34-35-36-37-38-39-30-31-32-33-34-35-36-37-38-39-30-31-32-33-34-35-36-37-38-39-30-31"
          ]
        },
        {
          "string": "12345678901234567890123456789012",
          "msgpack": [
            "d9-20-31-32-33-34-35-36-37-38-39-30-31-32-33-34-35-36-37-38-39-30-31-32-33-34-35-36-37-38-39-30-31-32",
            "da-00-20-31-32-33-34-35-36-37-38-39-30-31-32-33-34-35-36-37-38-39-30-31-32-33-34-35-36-37-38-39-30-31-32"
          ]
        }
      ],
      "31.string-utf8.yaml": [
        {
          "string": "Кириллица",
          "msgpack": [
            "b2-d0-9a-d0-b8-d1-80-d0-b8-d0-bb-d0-bb-d0-b8-d1-86-d0-b0",
            "d9-12-d0-9a-d0-b8-d1-80-d0-b8-d0-bb-d0-bb-d0-b8-d1-86-d0-b0"
          ]
        },
        {
          "string": "ひらがな",
          "msgpack": [
            "ac-e3-81-b2-e3-82-89-e3-81-8c-e3-81-aa",
            "d9-0c-e3-81-b2-e3-82-89-e3-81-8c-e3-81-aa"
          ]
        },
        {
          "string": "한글",
          "msgpack": [
            "a6-ed-95-9c-ea-b8-80",
            "d9-06-ed-95-9c-ea-b8-80"
          ]
        },
        {
          "string": "汉字",
          "msgpack": [
            "a6-e6-b1-89-e5-ad-97",
            "d9-06-e6-b1-89-e5-ad-97"
          ]
        },
        {
          "string": "漢字",
          "msgpack": [
            "a6-e6-bc-a2-e5-ad-97",
            "d9-06-e6-bc-a2-e5-ad-97"
          ]
        }
      ],
      "32.string-emoji.yaml": [
        {
          "string": "❤",
          "msgpack": [
            "a3-e2-9d-a4",
            "d9-03-e2-9d-a4"
          ]
        },
        {
          "string": "🍺",
          "msgpack": [
            "a4-f0-9f-8d-ba",
            "d9-04-f0-9f-8d-ba"
          ]
        }
      ],
      "40.array.yaml": [
        {
          "array": [],
          "msgpack": [
            "90",
            "dc-00-00",
            "dd-00-00-00-00"
          ]
        },
        {
          "array": [ 1 ],
          "msgpack": [
            "91-01",
            "dc-00-01-01",
            "dd-00-00-00-01-01"
          ]
        },
        {
          "array": [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 ],
          "msgpack": [
            "9f-01-02-03-04-05-06-07-08-09-0a-0b-0c-0d-0e-0f",
            "dc-00-0f-01-02-03-04-05-06-07-08-09-0a-0b-0c-0d-0e-0f",
            "dd-00-00-00-0f-01-02-03-04-05-06-07-08-09-0a-0b-0c-0d-0e-0f"
          ]
        },
        {
          "array": [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 ],
          "msgpack": [
            "dc-00-10-01-02-03-04-05-06-07-08-09-0a-0b-0c-0d-0e-0f-10",
            "dd-00-00-00-10-01-02-03-04-05-06-07-08-09-0a-0b-0c-0d-0e-0f-10"
          ]
        },
        {
          "array": [
            "a"
          ],
          "msgpack": [
            "91-a1-61",
            "dc-00-01-a1-61",
            "dd-00-00-00-01-a1-61"
          ]
        }
      ],
      "41.map.yaml": [
        {
          "map": {},
          "msgpack": [
            "80",
            "de-00-00",
            "df-00-00-00-00"
          ]
        },
        {
          "map": {
            "a": 1
          },
          "msgpack": [
            "81-a1-61-01",
            "de-00-01-a1-61-01",
            "df-00-00-00-01-a1-61-01"
          ]
        },
        {
          "map": {
            "a": "A"
          },
          "msgpack": [
            "81-a1-61-a1-41",
            "de-00-01-a1-61-a1-41",
            "df-00-00-00-01-a1-61-a1-41"
          ]
        }
      ],
      "42.nested.yaml": [
        {
          "array": [
            []
          ],
          "msgpack": [
            "91-90",
            "dc-00-01-dc-00-00",
            "dd-00-00-00-01-dd-00-00-00-00"
          ]
        },
        {
          "array": [
            {}
          ],
          "msgpack": [
            "91-80",
            "dc-00-01-80",
            "dd-00-00-00-01-80"
          ]
        },
        {
          "map": {
            "a": {}
          },
          "msgpack": [
            "81-a1-61-80",
            "de-00-01-a1-61-de-00-00",
            "df-00-00-00-01-a1-61-df-00-00-00-00"
          ]
        },
        {
          "map": {
            "a": []
          },
          "msgpack": [
            "81-a1-61-90",
            "de-00-01-a1-61-90",
            "df-00-00-00-01-a1-61-90"
          ]
        }
      ],
      "50.timestamp.yaml": [
        {
          "timestamp": [ 1514862245, 0 ],
          "msgpack": [
            "d6-ff-5a-4a-f6-a5"
          ]
        },
        {
          "timestamp": [ 1514862245, 678901234 ],
          "msgpack": [
            "d7-ff-a1-dc-d7-c8-5a-4a-f6-a5"
          ]
        },
        {
          "timestamp": [ 2147483647, 999999999 ],
          "msgpack": [
            "d7-ff-ee-6b-27-fc-7f-ff-ff-ff"
          ]
        },
        {
          "timestamp": [ 2147483648, 0 ],
          "msgpack": [
            "d6-ff-80-00-00-00"
          ]
        },
        {
          "timestamp": [ 2147483648, 1 ],
          "msgpack": [
            "d7-ff-00-00-00-04-80-00-00-00"
          ]
        },
        {
          "timestamp": [ 4294967295, 0 ],
          "msgpack": [
            "d6-ff-ff-ff-ff-ff"
          ]
        },
        {
          "timestamp": [ 4294967295, 999999999 ],
          "msgpack": [
            "d7-ff-ee-6b-27-fc-ff-ff-ff-ff"
          ]
        },
        {
          "timestamp": [ 4294967296, 0 ],
          "msgpack": [
            "d7-ff-00-00-00-01-00-00-00-00"
          ]
        },
        {
          "timestamp": [ 17179869183, 999999999 ],
          "msgpack": [
            "d7-ff-ee-6b-27-ff-ff-ff-ff-ff"
          ]
        },
        {
          "timestamp": [ 17179869184, 0 ],
          "msgpack": [
            "c7-0c-ff-00-00-00-00-00-00-00-04-00-00-00-00"
          ]
        },
        {
          "timestamp": [ -1, 0 ],
          "msgpack": [
            "c7-0c-ff-00-00-00-00-ff-ff-ff-ff-ff-ff-ff-ff"
          ]
        },
        {
          "timestamp": [ -1, 999999999 ],
          "msgpack": [
            "c7-0c-ff-3b-9a-c9-ff-ff-ff-ff-ff-ff-ff-ff-ff"
          ]
        },
        {
          "timestamp": [ 0, 0 ],
          "msgpack": [
            "d6-ff-00-00-00-00"
          ]
        },
        {
          "timestamp": [ 0, 1 ],
          "msgpack": [
            "d7-ff-00-00-00-04-00-00-00-00"
          ]
        },
        {
          "timestamp": [ 1, 0 ],
          "msgpack": [
            "d6-ff-00-00-00-01"
          ]
        },
        {
          "timestamp": [ -2208988801, 999999999 ],
          "msgpack": [
            "c7-0c-ff-3b-9a-c9-ff-ff-ff-ff-ff-7c-55-81-7f"
          ]
        },
        {
          "timestamp": [ -2208988800, 0 ],
          "msgpack": [
            "c7-0c-ff-00-00-00-00-ff-ff-ff-ff-7c-55-81-80"
          ]
        },
        {
          "timestamp": [ -62167219200, 0 ],
          "msgpack": [
            "c7-0c-ff-00-00-00-00-ff-ff-ff-f1-86-8b-84-00"
          ]
        },
        {
          "timestamp": [ 253402300799, 999999999 ],
          "msgpack": [
            "c7-0c-ff-3b-9a-c9-ff-00-00-00-3a-ff-f4-41-7f"
          ]
        }
      ],
      "60.ext.yaml": [
        {
          "ext": [ 1,
            "10"
          ],
          "msgpack": [
            "d4-01-10"
          ]
        },
        {
          "ext": [ 2,
            "20-21"
          ],
          "msgpack": [
            "d5-02-20-21"
          ]
        },
        {
          "ext": [ 3,
            "30-31-32-33"
          ],
          "msgpack": [
            "d6-03-30-31-32-33"
          ]
        },
        {
          "ext": [ 4,
            "40-41-42-43-44-45-46-47"
          ],
          "msgpack": [
            "d7-04-40-41-42-43-44-45-46-47"
          ]
        },
        {
          "ext": [ 5,
            "50-51-52-53-54-55-56-57-58-59-5a-5b-5c-5d-5e-5f"
          ],
          "msgpack": [
            "d8-05-50-51-52-53-54-55-56-57-58-59-5a-5b-5c-5d-5e-5f"
          ]
        },
        {
          "ext": [ 6,
            ""
          ],
          "msgpack": [
            "c7-00-06",
            "c8-00-00-06",
            "c9-00-00-00-00-06"
          ]
        },
        {
          "ext": [ 7,
            "70-71-72"
          ],
          "msgpack": [
            "c7-03-07-70-71-72",
            "c8-00-03-07-70-71-72",
            "c9-00-00-00-03-07-70-71-72"
          ]
        }
      ]
    }
  """)
}
