import {Configuration} from "@/api";
import {getBackendURL} from "@/utils/EnvironmentManager.ts";

export const apiConfig = new Configuration({
    basePath: getBackendURL()
})

export default apiConfig