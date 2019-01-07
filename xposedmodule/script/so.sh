result=$(ls build/intermediates/cmake/release/obj)|grep 'No such file or directory'
echo $result
if [ -z "${result}" ]; then
    echo '目录已生成'
    cp build/intermediates/cmake/release/obj/arm64-v8a/libnativeDump.so src/main/assets/nativeDumpV8a.so
    cp build/intermediates/cmake/release/obj/armeabi-v7a/libnativeDump.so src/main/assets/nativeDumpV7a.so
    cp build/intermediates/cmake/release/obj/armeabi/libnativeDump.so src/main/assets/nativeDump.so
    echo '完成'
else
    echo '目录未生成'
fi