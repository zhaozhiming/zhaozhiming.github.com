---
layout: post
title: 部署 AWS ECS 超详细攻略
date: 2024-10-04 22:10:05
description: 详尽介绍使用 AWS 角色在 GitHub Action 中部署 AWS ECS 服务
keywords: aws, role, ecs, github, github-action
comments: true
categories: code
tags: [aws, role, ecs, github, github-action]
---

{% img /images/post/2024/10/github-deploy-aws-ecs.jpg 400 300 %}

随着云计算和容器化的广泛普及，越来越多的团队选择使用 [AWS](https://aws.amazon.com/) ECS（Elastic Container Service）来运行他们的应用服务。同时，通过 GitHub Action 自动化 CI/CD 流程可以极大地提高开发效率。本文将详细介绍如何结合 GitHub Action 和 AWS ECS，将代码从仓库无缝部署到生产环境中。

<!--more-->

## 整体流程

{% img /images/post/2024/10/github-action-deploy-ecs-flow.png 1000 600 %}

在使用 GitHub Action 部署 AWS ECS 的过程中，整体流程可以分为以下几个步骤：

- 创建 ECR 资源：首先需要在 AWS ECR（Elastic Container Registry）中创建镜像仓库，用于存储应用程序的 Docker 镜像。通过 ECR，我们可以轻松地管理镜像的版本和更新，确保每次部署都能拉取到最新的稳定镜像。
- 配置 IAM 角色和权限：然后是配置好必要的 IAM 角色和权限，以便在整个流程中授权不同的服务执行所需的操作。这些角色包括 ECS 任务角色、执行角色以及 GitHub Action 的执行角色。
- 创建 ECS 资源：接着需要在 AWS 中创建 ECS 资源，包括 ECS 集群、服务和任务定义。这些资源用于运行容器化的应用程序，确保应用程序能够在容器中稳定运行。
- 创建 GitHub 工作流：在代码仓库中编写 GitHub Action 工作流，用于自动化构建和部署流程，通过 YAML 文件定义工作流步骤。

## 创建 ECR 资源

在部署 ECS 容器应用时，我们需要一个地方存放容器镜像，AWS ECR 正是用于存放 Docker 镜像的服务。AWS ECR 是一个完全托管的 Docker 容器注册表，集成了 AWS 的身份验证与访问控制，确保镜像的安全性。

ECR 提供了镜像仓库的功能，可以将应用的 Docker 镜像推送到 ECR 中。在部署 ECS 服务时，ECR 充当镜像存储库，ECS 会从中拉取镜像来启动任务。通过 ECR，我们可以轻松地管理镜像的版本和更新，确保每次部署都能拉取到最新的稳定镜像。同时，ECR 支持生命周期策略，可以自动清理旧的和不再使用的镜像，以节省存储成本。

在 AWS 管理控制台上创建镜像仓库的步骤如下：

- 登录到 AWS 管理控制台，然后导航到 Amazon ECR 服务页面。
- 在左侧导航栏中，选择 `Private registry` 或 `Public registry`，然后点击 `Repositories`。
- 点击右上角的 `Create repository` 按钮，如截图中所示。

{% img /images/post/2024/10/aws-ecr-create-repository.png 1000 600 %}

## 配置 IAM 角色和权限

在部署 AWS ECS 的过程中，有 3 种 IAM（Identity and Access Management） 角色需要配置：

- ECS 任务角色：用于授予 ECS 内部的任务权限，一般用来访问其他 AWS 服务，此角色应赋予容器内的应用程序所需的最小权限。
- ECS 执行角色：用于授予 ESC 执行的任务权限，一般是从 ECR 拉取镜像和写入 CloudWatch 日志，此角色同样应赋予最小权限。
- GitHub Action 执行角色：用于在 GitHub Action 中执行 AWS 操作，一般是推送镜像到 ECR 和更新 ECS 服务。

### ECS 任务角色

ECS 任务角色允许运行在 ECS 中的任务访问其他 AWS 服务。例如，如果任务中的容器需要访问 S3 存储，这时就需要配置合适的任务角色来访问 S3。任务角色的主要作用是授予容器内的应用程序权限，以便它们能够与其他 AWS 服务进行交互。为了提升安全性，任务角色应在满足应用程序需求的同时赋予最小权限。

以下是一个任务角色的最小权限策略示例，该角色允许操作 Lambda 函数和 Secrets Manager：

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": ["lambda:InvokeFunction", "lambda:GetFunctionConfiguration"],
      "Resource": "*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "secretsmanager:GetSecretValue",
        "secretsmanager:DescribeSecret"
      ],
      "Resource": "*"
    }
  ]
}
```

### ECS 执行角色

ECS 执行角色是 ECS 用来执行任务时的角色，主要用于拉取镜像（例如从 ECR 拉取镜像）以及写入 CloudWatch 日志。执行角色确保 ECS 能够顺利执行任务的相关操作，正确配置执行角色可以确保任务运行过程中能够顺利访问必要的资源，特别是在自动化部署和扩展过程中起到关键作用。

为了配置执行角色的最小权限，可以使用 AWS 的预定义策略 `AmazonECSTaskExecutionRolePolicy`（权限策略如下所示），这样可以确保角色具备最低限度的权限来完成必要的任务：

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "ecr:GetAuthorizationToken",
        "ecr:BatchCheckLayerAvailability",
        "ecr:GetDownloadUrlForLayer",
        "ecr:BatchGetImage",
        "logs:CreateLogStream",
        "logs:PutLogEvents"
      ],
      "Resource": "*"
    }
  ]
}
```

### GitHub Action 执行角色

在一些 Github Action 部署文档中（包括 [Github 官方的文档](https://docs.github.com/en/actions/use-cases-and-examples/deploying/deploying-to-amazon-elastic-container-service)），都会使用 AWS 的 IAM 用户来作为访问 AWS 的凭证（提供用户的 SECRET ACCESS KEY 和 SECRET ACCESS KEY），但**这种方式并不是最佳实践**，因为这样会暴露用户的凭证，更好的方式是创建一个角色来授权 Github Action 来访问 AWS 的资源。该角色需要有足够的权限来推送镜像到 ECR，并更新 ECS 服务的任务定义或服务。

以下是一个 GitHub Action 执行角色权限策略示例，并对示例中的策略进行说明：

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "VisualEditor0",
      "Effect": "Allow",
      "Action": "ecs:RegisterTaskDefinition",
      "Resource": "*"
    },
    {
      "Effect": "Allow",
      "Action": "iam:PassRole",
      "Resource": [
        "arn:aws:iam::<account-id>:role/<task-role>",
        "arn:aws:iam::<account-id>:role/<task-execution-role>"
      ]
    },
    {
      "Effect": "Allow",
      "Action": ["ecs:DescribeServices", "ecs:UpdateService"],
      "Resource": "arn:aws:ecs:<region>:<account-id>:service/<ecs-cluster>/<esc-service>"
    },
    {
      "Effect": "Allow",
      "Action": [
        "ecr:GetAuthorizationToken",
        "ecr:BatchCheckLayerAvailability",
        "ecr:InitiateLayerUpload",
        "ecr:UploadLayerPart",
        "ecr:CompleteLayerUpload",
        "ecr:PutImage"
      ],
      "Resource": "*"
    }
  ]
}
```

- ECS 任务定义注册权限：允许注册 ECS 任务定义，主要用于创建或更新任务定义，使其能够包含新的应用配置和容器信息。
- IAM 角色传递权限：允许将 IAM 角色传递给 ECS 服务。这意味着该角色可以在执行任务时传递其他 IAM 角色的权限，这里将权限传递给了我们之前创建的 2 个 ECS 角色——任务角色和执行角色，以便这些任务可以访问必要的 AWS 资源。
- ECS 服务描述和更新权限：允许查看指定 ECS 服务的状态和配置信息，允许更新 ECS 服务配置，例如更改使用的任务定义，或调整服务的副本数量。这里的资源填写的是 ECS 服务的 ARN，ECS 服务我们在下面会介绍如何创建。
- ECR 镜像操作权限：允许获取 ECR 访问令牌、检查镜像层是否存在、上传镜像层以及推送镜像到 ECR，以供 ECS 使用。

## 创建 ECS 资源

AWS ECS 是 AWS 提供的一种容器管理服务，它使得运行、停止和管理 Docker 容器变得更加简单。在部署 AWS ECS 服务之前，我们需要先创建 ECS 集群和服务。

### 创建集群和服务

- 进入 ECS 控制台，点击 `Create Cluster`，选择适合的运行类型（如 Fargate 或 EC2），配置集群的相关参数，确认后点击 `Create` 按钮完成集群的创建。
- 在集群创建完成后，点击 `Create Service`，选择服务类型，配置服务的相关参数，确认后点击 `Create` 按钮完成创建。

{% img /images/post/2024/10/aws-ecs-create-cluster.png 1000 600 %}

{% img /images/post/2024/10/aws-ecs-create-service.png 1000 600 %}

### 创建任务定义

任务定义是 ECS 中的核心概念之一，可以看作是应用的蓝图。它定义了容器的配置，包括镜像的来源、CPU 和内存的资源需求、端口配置等。每次启动任务时，都会根据任务定义运行相应的容器。任务定义的创建需要非常细致和准确，以确保部署的应用程序符合需求，例如可以设置环境变量、挂载卷和定义日志记录等。正确配置任务定义是确保应用程序在容器中稳定运行的基础。

以下是一个任务定义的 JSON 文件示例，并对示例中的代码进行说明：

```json
{
  "containerDefinitions": [
    {
      "name": "my-task-definition-name",
      "image": "<account-id>.dkr.ecr.<region>.amazonaws.com/<organization>/<image-name>:<image-tag>",
      "cpu": 0,
      "portMappings": [
        {
          "name": "supplier-8080-tcp-port",
          "containerPort": 8080,
          "hostPort": 8080,
          "protocol": "tcp",
          "appProtocol": "http"
        }
      ],
      "essential": true,
      "environment": [],
      "environmentFiles": [],
      "mountPoints": [],
      "volumesFrom": [],
      "ulimits": [],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/<ecs-service>",
          "awslogs-create-group": "true",
          "awslogs-region": "<region>",
          "awslogs-stream-prefix": "ecs"
        },
        "secretOptions": []
      },
      "systemControls": []
    }
  ],
  "family": "myserver-defs",
  "taskRoleArn": "arn:aws:iam::<account-id>:role/<task-role-name>",
  "executionRoleArn": "arn:aws:iam::<account-id>:role/<task-execution-role-name>",
  "networkMode": "awsvpc",
  "volumes": [],
  "placementConstraints": [],
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "2048",
  "runtimePlatform": {
    "cpuArchitecture": "X86_64",
    "operatingSystemFamily": "LINUX"
  },
  "tags": []
}
```

- containerDefinitions 属性：定义了容器的详细配置，包括容器名称、镜像来源、CPU 和内存的分配、端口映射等。在此示例中，容器名称为 `my-task-definition-name`，镜像是我们刚才创建的 ECR 镜像仓库中的镜像名称，并将容器的 8080 端口映射到主机的 8080 端口。
- logConfiguration 属性：指定了日志配置，使用 AWS CloudWatch Logs 记录容器的日志信息。日志组名称为 `/ecs/<ecs-service>`，并且在必要时会自动创建日志组。
- taskRoleArn 和 executionRoleArn 属性：分别指定了 ECS 任务角色和执行角色的 ARN，也就是我们刚才创建的 2 个 ECS 角色，它们用于控制容器内的应用访问其他 AWS 服务的权限，以及任务执行过程中所需的权限。
- networkMode 属性：指定网络模式为 `awsvpc`，表示容器将使用 AWS VPC 网络模式，这对于 Fargate 类型（无服务器计算引擎，用于简化容器部署）的任务是必需的。
- requiresCompatibilities 属性：指定任务兼容性为 `FARGATE`，表示此任务将在 AWS Fargate 上运行。
- cpu 和 memory 属性：定义了任务的 CPU 和内存配置。在此示例中，任务分配了 512 个 CPU 单位（0.5 个 vCPU）和 2048 MB 的内存。
- runtimePlatform 属性：指定了运行平台的架构和操作系统类型，确保任务在合适的环境中运行。

## 创建 GitHub 工作流

GitHub Action 是 GitHub 提供的自动化工作流工具，可以用于构建、测试和部署代码。在我们的场景中，GitHub Action 可以被用来自动化构建 Docker 镜像并将其推送到 ECR，然后更新 ECS 服务。通过 GitHub Action，开发人员可以定义一套自动化的工作流，从代码提交到服务部署，全程无需人工干预。

GitHub Action 的工作流通过配置文件（通常是 `.github/workflows` 目录下的 YAML 文件）进行定义。

我们首先来看下构建和推送 Docker 镜像到 ECR 镜像仓库的工作流步骤示例：

```yaml
build-push:
  name: ECR build and push
  needs: test
  runs-on: ubuntu-latest

  outputs:
    aws_ecr_image_tag: ${{ steps.build-push-step.outputs.aws_ecr_image_tag }}

  steps:
    - name: Checkout code
      uses: actions/checkout@v4

    - name: Configure AWS Credentials
      id: aws-credentials
      uses: aws-actions/configure-aws-credentials@v4
      with:
        role-to-assume: ${{ vars.AWS_GITHUB_ACTION_ROLE }}
        aws-region: ${{ vars.AWS_REGION }}

    - name: Log in to Amazon ECR
      id: ecr-login
      uses: aws-actions/amazon-ecr-login@v2
      with:
        mask-password: true
        registry-type: private

    - name: Build and push
      id: build-push-step
      env:
        AWS_ECR_REGISTRY: ${{ steps.ecr-login.outputs.registry }}
        AWS_ECR_REPOSITORY: ${{ vars.AWS_ECR_REPOSITORY }}
      run: |
        export AWS_ECR_IMAGE_TAG=$AWS_ECR_REGISTRY/$AWS_ECR_REPOSITORY:$(git rev-parse --short HEAD)	
        docker build -f ./server/build/Dockerfile -t $AWS_ECR_IMAGE_TAG ./server	
        docker push $AWS_ECR_IMAGE_TAG	
        echo "aws_ecr_image_tag=$AWS_ECR_IMAGE_TAG" >> "$GITHUB_OUTPUT"
```

- 从 GitHub 仓库中检出最新代码，以便进行构建。
- 配置 AWS 凭据，通过 `role-to-assume` 设定需要承担的 AWS 角色，以及目标区域。`role-to-assume` 填写的是我们之前创建的 Github Action 执行角色的 ARN，可以将其配置到 Github 仓库的 Variables 中，确保在工作流中可以使用这些信息。
- 登录到 AWS ECR，并确保凭据安全，使用的是上一步骤中配置的 AWS 凭据。
- 设置环境变量，如 ECR 注册表和镜像仓库名，环境变量的值同样可以配置到 Github 仓库的 Variables 中。然后生成包含 Git 提交短哈希的镜像标签，构建镜像并推送到 ECR，然后输出镜像标签供后续使用。

下面是 Github 仓库的 Secret 和  Variables 配置页面：

{% img /images/post/2024/10/github-setting-secret.png 1000 600 %}

我们再来看 ECS 部署的工作流步骤示例：

```yaml
ecs-deploy:
  name: ECS deployment
  needs: build-push
  runs-on: ubuntu-latest
  env:
    AWS_ECR_IMAGE_TAG: ${{ needs.build-push.outputs.aws_ecr_image_tag }}

  steps:
    - name: Checkout
      uses: actions/checkout@v4

    - name: Configure AWS Credentials
      id: aws-credentials
      uses: aws-actions/configure-aws-credentials@v4
      with:
        role-to-assume: ${{ vars.AWS_GITHUB_ACTION_ROLE }}
        aws-region: ${{ vars.AWS_REGION }}

    - name: Render Amazon ECS task definition
      id: render-task-def
      uses: aws-actions/amazon-ecs-render-task-definition@v1
      with:
        task-definition: your/task/definition/file.json
        container-name: ${{ vars.AWS_ECS_CONTAINER }}
        image: ${{ env.AWS_ECR_IMAGE_TAG }}
        environment-variables: |
          FOO=${{ vars.FOO }}

    - name: Deploy to Amazon ECS task definition
      id: deploy-task-def
      uses: aws-actions/amazon-ecs-deploy-task-definition@v1
      with:
        task-definition: ${{ steps.render-task-def.outputs.task-definition }}
        service: ${{ vars.AWS_ECS_SERVICE }}
        cluster: ${{ vars.AWS_ECS_CLUSTER }}
```

- 在 `ecs-deploy` 步骤中，将构建好的 Docker 镜像部署到 Amazon ECS 服务，依赖于前一步的 `build-push` 步骤，使用构建步骤的输出值 `aws_ecr_image_tag` 作为镜像标签。
- 检出代码，以便在部署过程中使用最新的代码版本。
- 配置 AWS 凭据，通过 `role-to-assume` 来承担之前配置好的 GitHub Action 执行角色，并指定 AWS 区域。
- 渲染任务定义文件，更新其中的容器镜像、环境变量等配置。这里的任务定义文件为 `your/task/definition/file.json`， 这个 JSON 文件相当于我们刚才介绍的任务定义示例文件，并替换容器镜像和其他必要的环境变量。
- 将渲染后的任务定义部署到指定的 ECS 服务和集群中，确保最新的代码和配置能够进行正确部署。这里用到了我们之前创建的 ECS Cluster 和 Service，可以将它们的名字配置到 Github 仓库的 Variables 中。

通过这种方式，代码一旦合并到主分支，就可以触发 GitHub Action 完成从构建到部署的一系列操作，极大地简化了手动部署的复杂度，同时保证了每次部署的一致性和可重复性。工作流的配置可以根据团队的需求进行扩展，例如加入单元测试、静态代码分析、通知等，以确保代码质量和部署的可靠性。

## 总结

本文介绍了部署 AWS ECS 所需的资源和权限，以及通过自动化工作流实现无缝部署。整体流程包括创建 ECR 资源、配置 IAM 角色和权限、创建 ECS 资源，以及编写 GitHub Action 工作流，实现从代码检出到容器部署的完整自动化。通过这种方式，开发团队可以提高部署效率，确保每次部署的一致性和可重复性，同时增强安全性和代码质量控制。

关注我，一起学习最新的开发编程新技术，欢迎交流，如果你有什么想问想说的，欢迎在评论区留言。
