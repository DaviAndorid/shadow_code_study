package com.example.dynamic_manager;


import com.example.dynamic_host.FailedException;
import com.example.dynamic_host.NotFoundException;
import com.example.dynamic_host.common.InstalledApk;

public interface UuidManagerImpl {
    InstalledApk getPlugin(String uuid, String partKey) throws NotFoundException, FailedException;

    InstalledApk getPluginLoader(String uuid) throws NotFoundException, FailedException;

    InstalledApk getRuntime(String uuid) throws NotFoundException, FailedException;
}
