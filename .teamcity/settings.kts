import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.dockerSupport
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.dockerCommand
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2019_2.projectFeatures.buildReportTab
import jetbrains.buildServer.configs.kotlin.v2019_2.projectFeatures.dockerRegistry
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2021.1"

project {
    description = "Contains all other projects"

    features {
        buildReportTab {
            id = "PROJECT_EXT_1"
            title = "Code Coverage"
            startPage = "coverage.zip!index.html"
        }
        dockerRegistry {
            id = "PROJECT_EXT_2"
            name = "Docker Registry"
            url = "https://docker.io"
            userName = "jastenewname"
            password = "credentialsJSON:eaba873d-78c0-4749-a8b5-8b4394e99b67"
        }
    }

    cleanup {
        baseRule {
            preventDependencyCleanup = false
        }
    }

    subProject(Autumn2021Backend)
    subProject(Autumn2021AndroidBaseApp)
    subProject(Autumn2021FlutterBaseApp)
}


object Autumn2021AndroidBaseApp : Project({
    name = "Autumn 2021 Android Base App"

    vcsRoot(Autumn2021AndroidBaseApp_HttpsGithubComGymHackathonTeamAutumn2021androidBaseAppGitRefsHeadsMaster)

    buildType(Autumn2021AndroidBaseApp_Build)
    buildType(Autumn2021AndroidBaseApp_DeployApk)
})

object Autumn2021AndroidBaseApp_Build : BuildType({
    name = "Build APK"

    artifactRules = "app/build/outputs/apk/debug/app-debug.apk"

    vcs {
        root(Autumn2021AndroidBaseApp_HttpsGithubComGymHackathonTeamAutumn2021androidBaseAppGitRefsHeadsMaster)
    }

    steps {
        gradle {
            name = "Build APK"
            tasks = "clean assembleDebug"
        }
    }
})

object Autumn2021AndroidBaseApp_DeployApk : BuildType({
    name = "Deploy APK"

    enablePersonalBuilds = false
    type = BuildTypeSettings.Type.DEPLOYMENT
    maxRunningBuilds = 1

    vcs {
        root(Autumn2021AndroidBaseApp_HttpsGithubComGymHackathonTeamAutumn2021androidBaseAppGitRefsHeadsMaster)
    }

    steps {
        step {
            name = "Upload APK"
            type = "ssh-deploy-runner"
            param("jetbrains.buildServer.deployer.username", "root")
            param("jetbrains.buildServer.deployer.sourcePath", "app-debug.apk")
            param("jetbrains.buildServer.deployer.targetUrl", "5.63.154.19:/root/autumn-2021-backend-launcher/artifacts/apk")
            param("secure:jetbrains.buildServer.deployer.password", "credentialsJSON:79af9490-7b13-4f2b-bbb4-0303b290b5a1")
            param("jetbrains.buildServer.sshexec.authMethod", "PWD")
            param("jetbrains.buildServer.deployer.ssh.transport", "jetbrains.buildServer.deployer.ssh.transport.scp")
        }
    }

    triggers {
        vcs {
            branchFilter = "+:refs/tags/*"
        }
    }

    dependencies {
        dependency(Autumn2021AndroidBaseApp_Build) {
            snapshot {
                onDependencyFailure = FailureAction.CANCEL
            }

            artifacts {
                artifactRules = "app-debug.apk"
            }
        }
    }
})

object Autumn2021AndroidBaseApp_HttpsGithubComGymHackathonTeamAutumn2021androidBaseAppGitRefsHeadsMaster : GitVcsRoot({
    name = "https://github.com/gym-hackathon-team/autumn-2021-android-base-app.git#refs/heads/master"
    url = "https://github.com/gym-hackathon-team/autumn-2021-android-base-app.git"
    branch = "refs/heads/master"
    branchSpec = """
        +:refs/heads/*
        +:refs/tags/*
    """.trimIndent()
    useTagsAsBranches = true
    authMethod = password {
        userName = "justrepo"
        password = "credentialsJSON:35ba3038-a703-4868-8686-ae79bf7a149c"
    }
})


object Autumn2021Backend : Project({
    name = "Autumn 2021 Backend"

    vcsRoot(Autumn2021Backend_HttpsGithubComGymHackathonTeamAutumn2021backendRefsHeadsDev)

    buildType(Autumn2021Backend_Deploy)
    buildType(Autumn2021Backend_BuildBff)
    buildType(Autumn2021Backend_BuildUser)
    buildType(Autumn2021Backend_BuildAuth)
})

object Autumn2021Backend_BuildAuth : BuildType({
    name = "Build Auth"

    vcs {
        root(Autumn2021Backend_HttpsGithubComGymHackathonTeamAutumn2021backendRefsHeadsDev, "auth/")
    }

    steps {
        dockerCommand {
            name = "Build Auth"
            commandType = build {
                source = file {
                    path = "auth/Dockerfile"
                }
                contextDir = "auth"
                namesAndTags = "jastenewname/gym-auth-service"
                commandArgs = "--pull"
            }
        }
        dockerCommand {
            name = "Push Auth"
            commandType = push {
                namesAndTags = "jastenewname/gym-auth-service"
            }
        }
    }

    features {
        dockerSupport {
            loginToRegistry = on {
                dockerRegistryId = "PROJECT_EXT_2"
            }
        }
    }
})

object Autumn2021Backend_BuildBff : BuildType({
    name = "Build BFF"

    vcs {
        root(Autumn2021Backend_HttpsGithubComGymHackathonTeamAutumn2021backendRefsHeadsDev, "bff/")
    }

    steps {
        dockerCommand {
            name = "Build BFF"
            commandType = build {
                source = file {
                    path = "bff/Dockerfile"
                }
                contextDir = "bff"
                namesAndTags = "jastenewname/gym-bff-service"
                commandArgs = "--pull"
            }
        }
        dockerCommand {
            name = "Push BFF"
            commandType = push {
                namesAndTags = "jastenewname/gym-bff-service"
            }
        }
    }

    features {
        dockerSupport {
            loginToRegistry = on {
                dockerRegistryId = "PROJECT_EXT_2"
            }
        }
    }
})

object Autumn2021Backend_BuildUser : BuildType({
    name = "Build User"

    vcs {
        root(Autumn2021Backend_HttpsGithubComGymHackathonTeamAutumn2021backendRefsHeadsDev, "user/")
    }

    steps {
        dockerCommand {
            name = "Build User"
            commandType = build {
                source = file {
                    path = "user/Dockerfile"
                }
                contextDir = "user"
                namesAndTags = "jastenewname/gym-user-service"
                commandArgs = "--pull"
            }
        }
        dockerCommand {
            name = "Push User"
            commandType = push {
                namesAndTags = "jastenewname/gym-user-service"
            }
        }
    }

    features {
        dockerSupport {
            loginToRegistry = on {
                dockerRegistryId = "PROJECT_EXT_2"
            }
        }
    }
})

object Autumn2021Backend_Deploy : BuildType({
    name = "Deploy"

    enablePersonalBuilds = false
    type = BuildTypeSettings.Type.DEPLOYMENT
    maxRunningBuilds = 1

    vcs {
        root(Autumn2021Backend_HttpsGithubComGymHackathonTeamAutumn2021backendRefsHeadsDev, "docker-compose.dev.yml", ".env.example")
    }

    steps {
        step {
            name = "Update configs"
            type = "ssh-exec-runner"
            param("jetbrains.buildServer.deployer.username", "root")
            param("jetbrains.buildServer.sshexec.command", """
                cd /root/autumn-2021-backend-launcher/
                git pull
            """.trimIndent())
            param("jetbrains.buildServer.deployer.targetUrl", "5.63.154.19")
            param("secure:jetbrains.buildServer.deployer.password", "credentialsJSON:79af9490-7b13-4f2b-bbb4-0303b290b5a1")
            param("jetbrains.buildServer.sshexec.authMethod", "PWD")
        }
        step {
            name = "Send docker-compose and .env"
            type = "ssh-deploy-runner"
            param("jetbrains.buildServer.deployer.username", "root")
            param("jetbrains.buildServer.deployer.sourcePath", """
                docker-compose.dev.yml
                .env.example
            """.trimIndent())
            param("jetbrains.buildServer.deployer.targetUrl", "5.63.154.19:/root/autumn-2021-backend-launcher/update")
            param("secure:jetbrains.buildServer.deployer.password", "credentialsJSON:79af9490-7b13-4f2b-bbb4-0303b290b5a1")
            param("jetbrains.buildServer.sshexec.authMethod", "PWD")
            param("jetbrains.buildServer.deployer.ssh.transport", "jetbrains.buildServer.deployer.ssh.transport.scp")
        }
        step {
            name = "Restart services"
            type = "ssh-exec-runner"
            param("jetbrains.buildServer.deployer.username", "root")
            param("jetbrains.buildServer.sshexec.command", """
                cd /root/autumn-2021-backend-launcher/
                docker-compose -f docker-compose.dev.yml down --remove-orphans
                ./update.sh || exit 1
                ./start.sh || exit 2
                docker image prune -f
            """.trimIndent())
            param("jetbrains.buildServer.deployer.targetUrl", "5.63.154.19")
            param("secure:jetbrains.buildServer.deployer.password", "credentialsJSON:79af9490-7b13-4f2b-bbb4-0303b290b5a1")
            param("jetbrains.buildServer.sshexec.authMethod", "PWD")
        }
    }

    triggers {
        vcs {
            branchFilter = "+:refs/tags/*"
        }
    }

    dependencies {
        snapshot(Autumn2021Backend_BuildAuth) {
            onDependencyFailure = FailureAction.CANCEL
        }
        snapshot(Autumn2021Backend_BuildBff) {
            onDependencyFailure = FailureAction.CANCEL
        }
        snapshot(Autumn2021Backend_BuildUser) {
            onDependencyFailure = FailureAction.CANCEL
        }
    }
})

object Autumn2021Backend_HttpsGithubComGymHackathonTeamAutumn2021backendRefsHeadsDev : GitVcsRoot({
    name = "https://github.com/gym-hackathon-team/autumn-2021-backend.git"
    url = "https://github.com/gym-hackathon-team/autumn-2021-backend.git"
    branch = "refs/heads/dev"
    branchSpec = "+:refs/tags/*"
    useTagsAsBranches = true
    authMethod = password {
        userName = "justrepo"
        password = "credentialsJSON:35ba3038-a703-4868-8686-ae79bf7a149c"
    }
})


object Autumn2021FlutterBaseApp : Project({
    name = "Autumn 2021 Flutter Base App"

    buildType(Autumn2021FlutterBaseApp_BuildApk)
    buildType(Autumn2021FlutterBaseApp_DeployWeb)
    buildType(Autumn2021FlutterBaseApp_BuildWeb)
    buildType(Autumn2021FlutterBaseApp_DeployApk)
})

object Autumn2021FlutterBaseApp_BuildApk : BuildType({
    name = "Build APK"

    artifactRules = "build/app/outputs/apk/release/app-release.apk"

    steps {
        script {
            name = "Build APK"
            scriptContent = "flutter build apk"
        }
    }
})

object Autumn2021FlutterBaseApp_BuildWeb : BuildType({
    name = "Build Web"

    artifactRules = "build/web"

    steps {
        script {
            name = "Build Web"
            scriptContent = "flutter build web --base-href=/web/"
        }
    }
})

object Autumn2021FlutterBaseApp_DeployApk : BuildType({
    name = "Deploy APK"

    enablePersonalBuilds = false
    type = BuildTypeSettings.Type.DEPLOYMENT
    maxRunningBuilds = 1

    steps {
        step {
            name = "Upload APK"
            type = "ssh-deploy-runner"
            param("jetbrains.buildServer.deployer.username", "root")
            param("jetbrains.buildServer.deployer.sourcePath", "app-release.apk")
            param("jetbrains.buildServer.deployer.targetUrl", "5.63.154.19:/root/autumn-2021-backend-launcher/artifacts/apk")
            param("secure:jetbrains.buildServer.deployer.password", "credentialsJSON:79af9490-7b13-4f2b-bbb4-0303b290b5a1")
            param("jetbrains.buildServer.sshexec.authMethod", "PWD")
            param("jetbrains.buildServer.deployer.ssh.transport", "jetbrains.buildServer.deployer.ssh.transport.scp")
        }
    }

    dependencies {
        dependency(Autumn2021FlutterBaseApp_BuildApk) {
            snapshot {
                onDependencyFailure = FailureAction.CANCEL
            }

            artifacts {
                artifactRules = "app-release.apk"
            }
        }
    }
})

object Autumn2021FlutterBaseApp_DeployWeb : BuildType({
    name = "Deploy Web"

    enablePersonalBuilds = false
    type = BuildTypeSettings.Type.DEPLOYMENT
    maxRunningBuilds = 1

    steps {
        step {
            name = "Upload Web"
            type = "ssh-deploy-runner"
            param("jetbrains.buildServer.deployer.username", "root")
            param("jetbrains.buildServer.deployer.sourcePath", "**/*")
            param("jetbrains.buildServer.deployer.targetUrl", "5.63.154.19:/root/autumn-2021-backend-launcher/artifacts/web")
            param("secure:jetbrains.buildServer.deployer.password", "credentialsJSON:79af9490-7b13-4f2b-bbb4-0303b290b5a1")
            param("jetbrains.buildServer.sshexec.authMethod", "PWD")
            param("jetbrains.buildServer.deployer.ssh.transport", "jetbrains.buildServer.deployer.ssh.transport.scp")
        }
    }

    dependencies {
        dependency(Autumn2021FlutterBaseApp_BuildWeb) {
            snapshot {
                onDependencyFailure = FailureAction.CANCEL
            }

            artifacts {
                artifactRules = "**/*"
            }
        }
    }
})
