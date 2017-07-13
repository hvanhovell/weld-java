extern crate jni;
extern crate weld;
extern crate libc;

use libc::c_void;

use jni::JNIEnv;
use jni::objects::{JClass, JString, JByteBuffer};
use jni::sys::{jstring, jlong, jint};
use jni::strings::*;

use weld::*;

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "C" fn Java_weld_WeldJNI_weld_1value_1new(_: JNIEnv, _: JClass, dataPtr: jlong) -> jlong {
    let data = dataPtr as *const c_void;
    let value = weld_value_new(data);
    value as jlong
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "C" fn Java_weld_WeldJNI_weld_1value_1pointer(_: JNIEnv, _: JClass, valuePtr: jlong) -> jlong {
    let value = valuePtr as *const WeldValue;
    let data = weld_value_data(value);
    data as jlong
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "C" fn Java_weld_WeldJNI_weld_1get_1buffer_1pointer(env: JNIEnv, _: JClass, buffer: JByteBuffer) -> jlong {
    let data = env.get_direct_buffer_address(buffer);
    data.unwrap().as_ptr() as jlong
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "C" fn Java_weld_WeldJNI_weld_1value_1run(_: JNIEnv, _: JClass, valuePtr: jlong) -> jlong {
    let value = valuePtr as *const WeldValue;
    weld_value_run(value)
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "C" fn Java_weld_WeldJNI_weld_1value_1free(_: JNIEnv, _: JClass, valuePtr: jlong) {
    let value = valuePtr as *mut WeldValue;
    weld_value_free(value)
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "C" fn Java_weld_WeldJNI_weld_1module_1compile(env: JNIEnv, _: JClass, jcode: JString, confPtr: jlong, errorPtr: jlong) -> jlong {
    let conf = confPtr as *const WeldConf;
    let error = errorPtr as *mut WeldError;
    let code = env.get_string(jcode).unwrap();
    let module = weld_module_compile(code.get_raw(), conf, error);
    module as jlong
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "C" fn Java_weld_WeldJNI_weld_1module_1run(_: JNIEnv, _: JClass, modulePtr: jlong, confPtr: jlong, inputPtr: jlong, errorPtr: jlong) -> jlong {
    let module = modulePtr as *mut WeldModule;
    let conf = confPtr as *const WeldConf;
    let input = inputPtr as *const WeldValue;
    let error = errorPtr as *mut WeldError;
    let result = weld_module_run(module, conf, input, error);
    result as jlong
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "C" fn Java_weld_WeldJNI_weld_1module_1free(_: JNIEnv, _: JClass, modulePtr: jlong) {
    let module = modulePtr as *mut WeldModule;
    weld_module_free(module)
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "C" fn Java_weld_WeldJNI_weld_1error_1new(_: JNIEnv, _: JClass) -> jlong {
    let error = weld_error_new();
    error as jlong
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "C" fn Java_weld_WeldJNI_weld_1error_1free(_: JNIEnv, _: JClass, errorPtr: jlong) {
    let error = errorPtr as *mut WeldError;
    weld_error_free(error)
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "C" fn Java_weld_WeldJNI_weld_1error_1code(_: JNIEnv, _: JClass, errorPtr: jlong) -> jint {
    let error = errorPtr as *mut WeldError;
    weld_error_code(error) as jint
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "C" fn Java_weld_WeldJNI_weld_1error_1message(env: JNIEnv, _: JClass, errorPtr: jlong) -> jstring {
    let error = errorPtr as *mut WeldError;
    let message = JNIStr::from_ptr(weld_error_message(error)).to_owned();
    env.new_string(message).unwrap().into_inner()
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "C" fn Java_weld_WeldJNI_weld_1conf_1new(_: JNIEnv, _: JClass) -> jlong {
    let conf = weld_conf_new();
    conf as jlong
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "C" fn Java_weld_WeldJNI_weld_1conf_1free(_: JNIEnv, _: JClass, confPtr: jlong) {
    let conf = confPtr as *mut WeldConf;
    weld_conf_free(conf)
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "C" fn Java_weld_WeldJNI_weld_1conf_1get(env: JNIEnv, _: JClass, confPtr: jlong, jkey: JString) -> jstring {
    let conf = confPtr as *const WeldConf;
    let key = env.get_string(jkey).unwrap();
    let valuePtr = weld_conf_get(conf, key.get_raw());
    if valuePtr.is_null() {
        std::ptr::null_mut()
    } else {
        let value = JNIStr::from_ptr(valuePtr).to_owned();
        env.new_string(value).unwrap().into_inner()
    }
}

#[no_mangle]
#[allow(non_snake_case)]
pub unsafe extern "C" fn Java_weld_WeldJNI_weld_1conf_1set(env: JNIEnv, _: JClass, confPtr: jlong, jkey: JString, jvalue: JString) {
    let conf = confPtr as *mut WeldConf;
    let key = env.get_string(jkey).unwrap();
    let value = env.get_string(jvalue).unwrap();
    weld_conf_set(conf, key.get_raw(), value.get_raw())
}
