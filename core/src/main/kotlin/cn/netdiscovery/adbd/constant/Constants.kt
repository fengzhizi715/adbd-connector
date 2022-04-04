package cn.netdiscovery.adbd.constant

/**
 *
 * @FileName:
 *          cn.netdiscovery.adbd.constant.Constants
 * @author: Tony Shen
 * @date: 2021-06-02 15:40
 * @version: V1.0 <描述当前版本功能>
 */
var DEFAULT_MODE = 436

const val A_VERSION = 0x01000001

const val A_VERSION_MIN = 0x01000000

const val A_VERSION_SKIP_CHECKSUM = 0x01000001

const val MAX_PAYLOAD = 1024 * 1024

const val ADB_AUTH_TOKEN = 1

const val ADB_AUTH_SIGNATURE = 2

const val ADB_AUTH_RSAPUBLICKEY = 3

const val TOKEN_SIZE = 20

val ASN1_PREAMBLE = byteArrayOf(0x00, 0x30, 0x21, 0x30, 0x09, 0x06, 0x05, 0x2B, 0x0E, 0x03, 0x02, 0x1A, 0x05, 0x00, 0x04, 0x14)

const val ADB_PROTOCOL_HEADER_SIZE = 24

const val SYNC_DATA_MAX = 64 * 1024

const val WRITE_DATA_MAX = 256 * 1024

const val S_IFMT = 61440
const val S_IRWXU = 448
const val S_IRUSR = 256
const val S_IWUSR = 128
const val S_IXUSR = 64
const val S_IRWXG = 56
const val S_IRGRP = 32
const val S_IWGRP = 16
const val S_IXGRP = 8
const val S_IRWXO = 7
const val S_IROTH = 4
const val S_IWOTH = 2
const val S_IXOTH = 1