---
layout: post
title: 使用 AWS 角色隔离资源和权限
date: 2024-10-03 16:56:11
description: 介绍如何使用 AWS 角色策略对资源和权限进行隔离
keywords: aws, role, policy, permission, go-lang
comments: true
categories: code
tags: [aws, role, policy, permission, go-lang]
---

{% img /images/post/2024/10/aws-role.jpg 400 300 %}

AWS 是目前全球最大的云服务提供商，提供了丰富的云服务，开发 Web 应用时经常会用到 AWS 的各种服务。在开发这种应用时，有时候开发人员为了图方便省事，只创建一个 AWS 角色来管理应用所涉及的所有 AWS 资源，这样不仅不利于 AWS 资源的管理，而且还会因为权限不当导致系统的安全性受到威胁。本文将介绍 AWS 角色中权限相关的内容，以及如何使用 AWS 角色策略对资源和权限进行隔离。

<!--more-->

## 背景介绍

假设一家初创公司在开发他们的电商平台时，决定使用 AWS 提供的几种服务，包括 Amazon S3 存储产品图片，Amazon RDS 存储用户数据，以及 Amazon Lambda 处理后台任务。为了快速开发和部署，开发人员创建了一个单一的 AWS 角色，这个角色被赋予了完全访问 S3、RDS 和 Lambda 的权限，以便可以无障碍地调用这些服务。这意味着所有用户共享相同的权限，没有进行细粒度的权限控制，导致潜在的安全隐患。

{% img /images/post/2024/10/source-aws-design.png 1000 600 %}

如果有坏人进入了这个应用程序，他们将能够访问所有的 AWS 资源，包括 S3、RDS 和 Lambda。这种情况下，坏人可以下载 S3 中所有的图片，删除 RDS 中的用户数据，或者通过 Lambda 执行恶意代码，造成严重的安全隐患。

{% img /images/post/2024/10/source-aws-design-danger.png 1000 600 %}

在这种情况下，我们可以使用 AWS 角色策略对资源和权限进行隔离。用户通过应用程序使用各自的 AWS 角色来访问 AWS 资源。每个角色被分配了相对应最低限度的权限，每个角色只能访问其对应的资源，而不能访问其他角色的资源。通过这种方式，每个用户只能访问与其相关的资源，避免了权限的滥用和安全隐患。

{% img /images/post/2024/10/complete-aws-design.png 1000 600 %}

## AWS 角色

在 AWS 的 IAM（Identity and Access Management）服务中，角色是一个 AWS 身份，它不是与特定用户或组关联的，而是与 AWS 服务关联的。角色定义了一个实体，它可以代表一个用户、一个应用程序或者一个服务，以便可以访问 AWS 资源。角色可以通过权限策略来控制对资源的访问权限，以及信任关系来控制角色的使用者。

### 权限属性

在日常使用中，*权限策略（Permission Policy）*和*信任关系（Trust Relationship）*是 AWS 角色权限最常用和最核心的两个属性。

**权限策略**

- 定义：权限策略是一个以 JSON 格式编写的文档，定义了角色被授予的具体权限。它描述了角色可以对哪些 AWS 资源执行哪些操作。
- 作用：控制角色在 AWS 中*可以做什么*。例如，允许角色访问特定的 S3 存储桶、启动 EC2 实例、或调用特定的 Lambda 函数等。

**信任关系**

- 定义：信任关系也是一个以 JSON 格式编写的文档，定义了哪些实体（如用户、角色、服务）被允许扮演（Assume） 该角色。它指定了可以获得该角色临时凭证的主体。
- 作用：控制*谁可以扮演这个角色*。例如，允许特定的 IAM 用户、其他角色或者 AWS 服务（如 EC2、Lambda）来扮演该角色。

这两者共同构成了 IAM 角色的安全模型，权限策略确保角色只能执行被授予的操作，信任关系确保只有被信任的主体才能扮演该角色。

### 操作资源

AWS 角色可以被授予访问任何 AWS 服务和资源的权限，具体取决于添加到该角色的权限策略，可操作的资源包括但不限于：

- AWS Lambda：创建、更新、调用函数；配置触发器和日志等。
- Amazon EventBridge Scheduler：可以按照预定的时间或间隔来调度任务或操作。
- AWS KMS（密钥管理服务）：创建和管理加密密钥；控制密钥的使用权限等

还有很多其他 AWS 服务和资源没有列出来，这里只列出比较常用的几种 AWS 资源，我们在下面的示例也会着重介绍如何通过角色策略来控制这些资源的访问权限。

## 添加信任关系

在 AWS 角色中，信任关系是一个非常重要的属性，它定义了哪些实体被允许扮演该角色。信任关系是一个以 JSON 格式编写的文档，它指定了可以获得该角色临时凭证的主体。这些实体可以是 AWS 服务或者 IAM 用户 。

下面的信任关系中添加了 `event`、`lambda` 和 `scheduler` 3 个 AWS 服务，这意味着这些服务可以扮演该角色。

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": [
          "events.amazonaws.com",
          "scheduler.amazonaws.com",
          "lambda.amazonaws.com"
        ]
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
```

下面的信任关系中添加了 IAM 用户 `user1` 作为扮演该角色的主体，这意味着 `user1` 可以扮演该角色。

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "AWS": "arn:aws:iam::123456789012:user/user1@org1.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
```

其中 `Statement` 是一个数组，这意味着你可以将多个实体添加到信任关系中，比如同时添加其他 AWS 服务和 IAM 用户。

## 添加权限策略

在 AWS 角色中添加权限策略是控制角色对资源的访问权限的关键，我们可以通过以下两种方式来添加权限策略：

{% img /images/post/2024/10/aws-role-permission.png 1000 600 %}

### 添加预定义策略

AWS 提供了一些预定义的策略，可以直接添加到角色中，这些策略包括了一些常用的权限，比如只读、只写、完全访问等。以 Lambda 服务为例，你可以在 AWS 控制台中选择`AWSLambda_FullAccess`、`AWSLambda_ReadOnlyAccess`、`AWSLambdaBasicExecutionRole` 等预定义策略，然后将其添加到角色中。

添加预定义策略的好处是你不必了解所有的权限细节，比如你要添加 Lambda 函数的权限，你可以直接添加 `AWSLambda_FullAccess` 策略，这样就可以访问 Lambda 服务的所有权限。虽然这样做十分方便，，但是每个预定义策略的权限范围可能比你所需的权限更广泛或者更窄，甚至有时候为了添加几个不同资源的权限而添加了很多个预定义策略。

### 创建自定义策略

自定义策略就比预定义策略灵活的多，你可以根据自己的需求将不同 AWS 服务的权限添加到一个策略中，比如下面这个例子：

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": ["lambda:InvokeFunction", "lambda:GetFunction"],
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

这个自定义策略允许角色执行 Lambda 函数的 `InvokeFunction` 和 `GetFunction` 操作，同时允许角色获取 Secrets Manager 中的 `GetSecretValue` 和 `DescribeSecret` 操作。不必添加诸如 AWS Lambda 和 Secrets Manager 的预定义策略，而且权限范围更加精准，更加符合你的实际需求。

## 权限隔离

下面我们再来介绍一下如何为 AWS 角色或者资源进行权限隔离，以保证资源和权限的安全性，下面以 Lambda 函数和 Secrets Manager 这两种资源为例进行介绍。

### Lambda 函数

在刚才的示例中，我们为角色添加了访问 Lambda 函数的权限策略，这意味着角色可以调用 Lambda 函数，但如果我们想控制角色**只能调用特定的 Lambda 函数**，那么我们就需要在权限策略中添加特定的 Lambda 函数的 ARN（Amazon 资源名称）。下面是一个示例：

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": ["lambda:InvokeFunction", "lambda:GetFunction"],
      "Resource": "arn:aws:lambda:us-west-1:123456789012:function:role1-*"
    }
  ]
}
```

我们可以使用角色名称作为前缀来命名 Lambda 函数，然后在权限策略中添加限制，仅能调用以该前缀命名的 Lambda 函数，通过这种方式，可以保证该角色仅能调用属于自己的 Lambda 函数。假设我们的 AWS 角色名称为 `role1`，那么我们可以将 Lambda 函数的名称以 `role1-` 作为前缀，然后在角色的权限策略中添加只能调用以 `role1-` 为前缀的 Lambda 函数，这样就不会担心该角色调用其他 Lambda 函数。

还有另外一种方法，我们也可以在 Lambda 函数权限中设置可访问的角色，这样确保该 Lambda 函数只能被某个角色调用。在 AWS 上设置 Lambda 函数权限的具体路径为：`Lambda -> Functions -> Your Lambda Function -> Configuration -> Permissions -> Add Permission`。

{% img /images/post/2024/10/aws-lambda-func-permission.png 1000 600 %}

这两种方式都可以起到相同的作用，选择哪种方式取决于你的具体需求。当然你也可以同时使用这两种方式，这样可以让权限更加精确，但请确保这两者之间的权限不冲突。AWS 将同时评估两者，只有在两者都允许的情况下，访问才会被授予。

### Secret Manager

为 Secrets Manager 添加权限策略也是一样的，我们在角色的权限策略中对资源进行限制，让其只能访问特定的 Secret，而不能访问其他 Secret。下面是一个示例：

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "secretsmanager:GetSecretValue",
        "secretsmanager:DescribeSecret"
      ],
      "Resource": "arn:aws:secretsmanager:us-west-1:123456789012:secret:role1-*"
    }
  ]
}
```

我们将 Secret 的名字同样以角色名称为前缀，然后在权限策略中添加权限，只能访问以该前缀命名的 Secret，这样就可以确保该角色只能访问属于自己的 Secret。

同样地，我们也可以在 Secret 的权限中设置中可访问的角色，这样确保该 Secret 只能被某些角色访问。相比 Lambda 函数，Secret 的权限策略更加灵活，Secret 可以支持设置多个角色对其进行访问，而 Lambda 函数只能设置一个角色。下面是 Secret 的权限设置示例：

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "AWS": ["arn:aws:iam::123456789012:role/role1"]
      },
      "Action": [
        "secretsmanager:GetSecretValue",
        "secretsmanager:DescribeSecret"
      ],
      "Resource": "arn:aws:secretsmanager:us-west-1:123456789012:secret:role1-SEC-wBO2wW"
    },
    {
      "Effect": "Deny",
      "Principal": "*",
      "Action": ["secretsmanager:GetSecretValue"],
      "Resource": "arn:aws:secretsmanager:us-west-1:123456789012:secret:role1-SEC-wBO2wW",
      "Condition": {
        "StringNotEquals": {
          "aws:PrincipalArn": ["arn:aws:iam::123456789012:role/role1"]
        }
      }
    }
  ]
}
```

在这个 Secret 的权限设置中，我们允许 `role1` 角色访问该 Secret，同时拒绝其他角色访问该 Secret。这样就保证了只有 `role1` 角色可以访问该 Secret，其他角色无法访问。

## 代码示例

如果手动来创建角色并设置权限策略，可能会比较繁琐，特别是当需要创建多个角色和资源的时候。为了提高效率，我们可以使用 AWS SDK 来快速执行这些操作。

下面我们通过 Go 语言来演示如何使用 [AWS SDK](https://aws.amazon.com/sdk-for-go/) 来创建角色、Lambda 函数和 Secret，并设置权限策略。

### 创建角色并设置权限策略

```go
package foo

import (
    "context"
    "errors"
    "fmt"

    "github.com/aws/aws-sdk-go/aws"
    "github.com/aws/aws-sdk-go/service/iam"
)

func CreateAwsRole(c context.Context, amazonConfig aws.Config) (*iam.CreateRoleOutput, error) {
	roleName := "role1"
	assumeRolePolicyDocument := `{
	"Version": "2012-10-17",
	"Statement": [
		{
			"Effect": "Allow",
			"Principal": {
				"Service": [
					"lambda.amazonaws.com",
				]
			},
			"Action": "sts:AssumeRole"
		}
	]
}`
	createRoleInput := &iam.CreateRoleInput{
		RoleName:                 aws.String(roleName),
		AssumeRolePolicyDocument: aws.String(assumeRolePolicyDocument),
	}

	iamClient := iam.NewFromConfig(amazonConfig)
	role, err := iamClient.CreateRole(c, createRoleInput)
	if err != nil {
		return nil, errors.New(fmt.Sprintf("create aws role occurred error: %s", err.Error()))
	}

    return role, err
```

- 在 `CreateRole` 的方法中，我们创建了一个名为 `role1` 的 AWS 角色，该角色的信任关系允许 Lambda 服务扮演该角色，这意味着该角色下的 Lambda 函数拥有该角色资源权限，比如该角色下的 Lambda 函数可以访问该角色下的 Secret。
- 创建 AWS 角色需要 2 个参数，一个是角色名称 `role1`，另一个是信任关系文档 `assumeRolePolicyDocument`。

然后我们再为这个角色添加权限策略：

```go
func PutRolePolicy(c context.Context, amazonConfig aws.Config, roleName string) (error) {
	policyDocument := `{
	"Version": "2012-10-17",
	"Statement": [
		{
			"Effect": "Allow",
			"Action": [
				"lambda:InvokeFunction",
				"lambda:GetFunction"
			],
			"Resource": "arn:aws:lambda:us-west-1:123456789012:function:role1-*"
		},
		{
			"Effect": "Allow",
			"Action": [
				"secretsmanager:GetSecretValue",
				"secretsmanager:DescribeSecret"
			],
            "Resource": "arn:aws:secretsmanager:us-west-1:123456789012:secret:role1-*"
	]
}`,
	putRolePolicyInput := &iam.PutRolePolicyInput{
		PolicyName:     aws.String(fmt.Sprintf("%s-policy", roleName)),
		PolicyDocument: aws.String(policyDocument),
		RoleName:       aws.String(roleName),
	}

	iamClient := iam.NewFromConfig(amazonConfig)
	_, err = iamClient.PutRolePolicy(c, putRolePolicyInput)
	if err != nil {
		return errors.New(fmt.Sprintf("put role policy for aws role occurred error: %s", err.Error()))
	}

	return nil
}
```

- 在 `PutRolePolicy` 的方法中，我们为 `role1` 角色添加了权限策略
- 权限策略中允许该角色调用名称以 `role1-` 为前缀的 Lambda 函数
- 权限策略中允许该角色访问名称以 `role1-` 为前缀的 Secret
- 创建权限策略需要 3 个参数，一个是角色名称 `role1`，另一个是权限策略文档 `policyDocument`，最后一个是权限策略名称，这里我们使用角色名称加上 `-policy` 作为权限策略名称。

### 创建 Lambda 函数并设置角色权限

```go
import (
    "context"
    "errors"
    "fmt"
    "strings"

    "github.com/aws/aws-sdk-go/aws"
    "github.com/aws/aws-sdk-go/service/lambda"
    lambTypes "github.com/aws/aws-sdk-go/service/lambda/types"
)

type ReqFile struct {
    _     struct{}
    Name  string
    Bytes []byte
}

func CreateLambdaFunction(c context.Context, file *ReqFile, roleName string, roleARN string, amazonConfig aws.Config) (*lambda.CreateFunctionOutput, error) {
	splits := strings.Split(file.Name, ".")
	fileName := splits[0]

	lambdaClient := lambda.NewFromConfig(amazonConfig)
	lambdaFun, err := lambdaClient.RegisterLambda(
		c,
		&lambda.CreateFunctionInput{
			Code: &lambTypes.FunctionCode{
				ZipFile: file.Bytes,
			},
			FunctionName: aws.String(fmt.Sprintf("%s-%s", roleName, fileName)),
			Role:        aws.String(roleARN),
			Runtime:     lambTypes.RuntimeNodejs20x,
			Timeout:     aws.Int32(30),
			Description: nil,
			Handler:     aws.String(fmt.Sprintf("%s.handler", fileName)),
			PackageType: lambTypes.PackageTypeZip,
			Publish:     false,
		},
		func(opt *lambda.Options) {},
	)
	if err != nil {
		return nil, errors.New(fmt.Sprintf("failed to create lambda function: %s, err: %s", fileName, err.Error()))
	}

	return lambdaFun, nil
}
```

- 在 `CreateLambdaFunction` 的方法中，我们创建了一个 Lambda 函数
- AWS SDK 的 `RegisterLambda` 方法需要多个参数，其中 `Role` 参数是角色的 ARN，就是我们刚才创建的那个角色
- Lambda 函数需要一个 Zip 格式的文件，这个文件包含了 Lambda 函数的代码，代码中的主要执行函数是 `handler`，这个函数需要在 `Handler` 参数中指定
- Lambda 函数的名称是角色名称加上文件名作为前缀，这样就保证了 Lambda 函数只能被该角色调用

### 创建 Secret 并设置角色权限

```go
import (
    "context"
    "errors"
    "fmt"

    "github.com/aws/aws-sdk-go/aws"
    "github.com/aws/aws-sdk-go/service/secretsmanager"
)

func CreateAwsSecretKey(
	c context.Context,
	awsRole *iam.CreateRoleOutput,
	amazonConfig aws.Config,
) (*secretsmanager.CreateSecretOutput, error) {
	secretName := fmt.Sprintf("%s-Sec", awsRole.Name)
	secretValue := "secret-value"
	input := &secretsmanager.CreateSecretInput{
		Name:         aws.String(secretName),
		SecretString: aws.String(secretValue),
	}

	secretManagerClient := secretsmanager.NewFromConfig(amazonConfig)
	awsSecretKey, err := secretManagerClient.CreateSecret(c, input)
	if err != nil {
		return nil, errors.New(fmt.Sprintf("create aws secret key occurred error: %s", err.Error()))
	}

    return awsSecretKey, nil
}
```

- 在 `CreateAwsSecretKey` 的方法中，我们创建了一个 Secret
- 参数 `awsRole` 是我们刚才创建的 AWS 角色，包含了角色的名称和 ARN
- 创建 Secret 需要 2 个参数，一个是 Secret 名称，另一个是 Secret 值
- 创建 Secret 名称时，我们使用角色名称加上 `-Sec` 作为 Secret 名称，这样就保证了 Secret 只能被该角色访问

然后我们再为这个 Secret 添加资源策略：

```go
func PutResourcePolicyForAwsSecretKey(
	c context.Context,
	roleARN string,
	awsSecretKey *secretsmanager.CreateSecretOutput,
	amazonConfig aws.Config,
) error {
	resourcePolicy := fmt.Sprintf(`{
		"Version": "2012-10-17",
		"Statement": [
			{
				"Effect": "Allow",
				"Principal": {
					"AWS": [ "%s"  ]
				},
				"Action": [ "secretsmanager:GetSecretValue", "secretsmanager:DescribeSecret" ],
				"Resource": "%s"
			},
			{
				"Effect": "Deny",
				"Principal" : "*",
				"Action" : "secretsmanager:GetSecretValue",
				"Resource" : "%s",
				"Condition": {
					"StringNotEquals": {
						"aws:PrincipalArn": [ "%s" ]
					}
				}
			}
		]
	}`,
        roleARN,
        awsSecretKey.ARN,
        awsSecretKey.ARN,
        roleARN,
    )
	policyInput := &secretsmanager.PutResourcePolicyInput{
		SecretId:       aws.String(awsSecretKey.Name),
		ResourcePolicy: aws.String(resourcePolicy),
	}

	secretManagerClient = secretsmanager.NewFromConfig(amazonConfig)
	_, err = secretManagerClient.PutResourcePolicy(c, policyInput)
	if err != nil {
		return errors.New(fmt.Sprintf("put resource policy for aws secret key occurred error: %s", err.Error()))
	}

	return nil
}
```

- 在 `PutResourcePolicyForAwsSecretKey` 的方法中，我们为 Secret 添加了资源策略
- 参数 `awsSecret` 是我们刚才创建的 AWS Secret，包含了 Secret 的名称和 ARN 等信息
- 权限策略中允许 `role1` 角色访问该 Secret，同时拒绝其他角色访问该 Secret
- 添加 Secret 权限策略需要 2 个参数，一个是 Secret 名字，另一个是权限策略文档 `resourcePolicy`

## 总结

今天我们讨论了在开发 AWS 应用时遇到的普遍问题，以及解决这一问题的主要方法。在这个过程中，我们还介绍了 AWS 角色的权限属性，包括权限策略和信任关系，以及如何使用这些属性来隔离资源。最后我们通过 Go 语言代码示例演示了如何创建 AWS 角色、Lambda 函数和 Secret，并设置权限策略。通过代码的方式，我们可以快速创建和设置 AWS 资源，保证资源和权限的安全性。

关注我，一起学习最新的开发编程新技术，欢迎交流，如果你有什么想问想说的，欢迎在评论区留言。
