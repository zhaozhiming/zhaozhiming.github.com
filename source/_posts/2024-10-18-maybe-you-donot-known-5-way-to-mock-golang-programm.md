---
layout: post
title: Go 开发中你应该了解的 5 种 Mock 方法
date: 2024-10-18 09:17:48
description: 介绍在 Golang 中对不同测试对象进行 Mock 的 5 种方法
keywords: golang, mock, unit-test, sql-mock, httpmock
comments: true
categories: code
tags: [golang, mock, unit-test, sql-mock, httpmock]
---

{% img /images/post/2024/10/golang-mock-tech.jpeg 400 300 %}

在软件开发过程中，单元测试是确保代码质量的重要环节，而在编写单元测试时，我们通常需要隔离待测试的代码与其依赖的外部组件，例如引用的外部方法、数据库等。Mock 技术可以帮助我们模拟这些外部组件，控制它们的行为和输出，从而让我们可以专注于测试目标代码的逻辑。本文将介绍在 Golang 中常用的 5 种 Mock 方法，帮助你在编写单元测试时更加得心应手。

<!--more-->

## Testify Mock

[Testify](https://github.com/stretchr/testify) 是 Go 生态中一个非常流行的测试工具库，主要用于简化 Go 语言中的单元测试工作。它是一个包含多个包的集合，提供了断言、Mock 和其他便捷的测试功能。

Testify 的 Mock 是其中一个功能强大的模块，它提供了模拟接口和方法调用的能力，支持参数匹配、调用次数和顺序的验证。它允许开发者通过链式调用设置不同的返回值和行为，并能精确匹配参数或自定义参数验证，帮助模拟外部依赖如 API 调用，使其在复杂的逻辑测试中非常灵活和高效。它与 Go 原生测试框架无缝集成，提供直观易用的 API，简化了测试代码编写和维护。

下面我们通过代码示例介绍 Testify Mock 的功能，首先来看被测试的函数：

```go
package foo

import (
	"context"
	"encoding/json"

	"github.com/org/proj/server/third-party/amazonx"

	"github.com/aws/aws-sdk-go-v2/aws"
	"github.com/aws/aws-sdk-go-v2/service/secretsmanager"
)

type (
	service struct {
		amazon amazonx.Amazon
	}
)

func (svc *service) GetTokenFromAWSSecret(c context.Context) (string, error) {
	input := &secretsmanager.GetSecretValueInput{
		SecretId:     aws.String("SECRET_ID"),
		VersionStage: aws.String("AWSCURRENT"),
	}

	resp, err := svc.amazon.GetSecretValue(c, input)
	if err != nil {
		return "", err
	}

	resMap := make(map[string]interface{})
	if err := json.Unmarshal([]byte(*resp.SecretString), &resMap); err != nil {
		return "", err
	}

	return resMap["token"].(string), nil
}
```

- 被测试方法 `GetTokenFromAWSSecret` 输入参数是一个 `context.Context` 对象，返回一个 `string` 类型的 `token` 和一个 `error` 对象
- `GetTokenFromAWSSecret` 方法依赖于 `amazonx.Amazon` 接口，通过 `svc.amazon.GetSecretValue` 方法获取 AWS Secret Value，并解析其中的 `token` 字段。

了解完被测函数后， 我们来看如何使用 Testify Mock 对 `amazonx.Amazon` 接口进行 Mock：

```go
package foo

import (
    "context"

    "github.com/aws/aws-sdk-go-v2/service/secretsmanager"
    "github.com/stretchr/testify/mock"
)

type MockAmazon struct {
	mock.Mock
}

func (m *MockAmazon) GetSecretValue(ctx context.Context, input *secretsmanager.GetSecretValueInput) (*secretsmanager.GetSecretValueOutput, error) {
	args := m.Called(ctx, input)
	return args.Get(0).(*secretsmanager.GetSecretValueOutput), args.Error(1)
}
```

- 首先我们使用 Testify Mock 定义了一个 `MockAmazon` 结构体
- 然后使用 Mock 对象来实现 `amazonx.Amazon` 接口的 `GetSecretValue` 方法

接下来我们编写单元测试代码：

```go
package foo

import (
	"context"
	"testing"

	"github.com/aws/aws-sdk-go-v2/service/secretsmanager"
	"github.com/stretchr/testify/assert"
)

func TestGetTokenFromAWSSecretSuccess(t *testing.T) {
	mockAmazon := new(MockAmazon)
	svc := &service{
		amazon: mockAmazon,
	}

	ctx := context.TODO()
	secretString := `{"token": "test-token"}`
	mockAmazon.On("GetSecretValue", ctx,
	    mock.AnythingOfType("*secretsmanager.GetSecretValueInput")).
	Return(&secretsmanager.GetSecretValueOutput{
	    SecretString: &secretString,
	}, nil)

	token, err := svc.GetTokenFromAWSSecret(ctx)
	assert.NoError(t, err)
	assert.Equal(t, "test-token", token)
}
```

- 在测试函数中，我们首先创建了一个 `MockAmazon` 对象，并将其传入 `service` 结构体中
- 然后模拟 `GetSecretValue` 方法的行为，设置返回值为 `{ "token": "test-token" }`
- 最后调用 `GetTokenFromAWSSecret` 方法，验证返回值是否符合预期

这里需要注意的地方是，`mockAmazon` 对象需要模拟真实对象 `amazonx.Amazon` 的**所有方法**，如果有其中一个方法没有实现的话，就**无法赋值**给 `service` 结构体。一旦真实对象的接口方法很多，那么手动实现所有方法就会十分繁琐，这个时候如果有一种工具可以自动生成 Mock 方法就会非常方便，这就是我们接下来要介绍的 Mock 工具——GoMock。

## GoMock

[GoMock](https://github.com/uber-go/mock) 是一个为 Go 语言提供模拟框架的工具库，由 Uber 维护，支持 Go 官方支持的最新两个版本。GoMock 允许开发者使用 `mockgen` 工具来生成用于测试的模拟对象，支持源文件模式和包模式生成模拟对象，并且与 Go 的内置 `testing` 包兼容，同时支持类型安全的模拟方法，并允许设置详细的期望调用。

使用 GoMock 首先需要安装 `mockgen` 工具：

```bash
go install github.com/golang/mock/mockgen@latest
```

然后在要模拟的接口文件中添加接口生成注释，以之前的被测方法为例，我们需要在 `amazonx` 包中的 `amazon.go` 文件中添加如下注释：

```go
//go:generate mockgen -destination ./amazon_mock.go -package amazonx -source amazon.go Amazon
```

这个注释表示使用 `mockgen` 在当前目录下生成一个 `amazon_mock.go` 文件，包名为 `amazonx`，并且使用 `amazon.go` 文件中的 `Amazon` 接口生成 Mock 对象。一般支持 Golang 的编辑器都支持 `go:generate` 注释，可以直接在编辑器中执行生成 Mock 对象的命令。

{% img /images/post/2024/10/golang-mockgen.png 1000 600 %}

生成后的 `amazon_mock.go` 文件内容大概是这个样子：

```go
// MockAmazon is a mock of Amazon interface.
type MockAmazon struct {
	ctrl     *gomock.Controller
	recorder *MockAmazonMockRecorder
}

// MockAmazonMockRecorder is the mock recorder for MockAmazon.
type MockAmazonMockRecorder struct {
	mock *MockAmazon
}

// NewMockAmazon creates a new mock instance.
func NewMockAmazon(ctrl *gomock.Controller) *MockAmazon {
	mock := &MockAmazon{ctrl: ctrl}
	mock.recorder = &MockAmazonMockRecorder{mock}
	return mock
}

// Other methods...
```

我们可以使用 `NewMockAmazon` 方法来模拟 `amazonx.Amazon` 接口，下面来看下我们使用 GoMock 写的测试代码：

```go
package foo

import (
	"context"
	"testing"

	"github.com/org/proj/server/third-party/amazonx"

	"github.com/golang/mock/gomock"
	"github.com/stretchr/testify/assert"
)

func TestGetTokenFromAWSSecretByGomock(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockAmazon := amazonx.NewMockAmazon(ctrl)
	svc := &service{
		amazon: mockAmazon,
	}

	ctx := context.TODO()

	secretString := `{"token":"test-token"}`
	mockAmazon.EXPECT().GetSecretValue(ctx, gomock.Any()).
		Return(&secretsmanager.GetSecretValueOutput{
			SecretString: &secretString,
		}, nil)

	token, err := svc.GetTokenFromAWSSecret(ctx)
	assert.NoError(t, err)
	assert.Equal(t, "test-token", token)
}
```

- 在测试函数中，我们首先创建了一个 GoMock 的 `controller` 对象，然后使用 `amazonx.NewMockAmazon` 方法创建一个 `MockAmazon` 对象
- 然后通过 `EXPECT` 方法设置 `GetSecretValue` 方法的行为和返回值
- 最后调用 `GetTokenFromAWSSecret` 方法，验证返回值是否符合预期

使用 GoMock 写的测试代码跟 Testify Mock 类似，但是我们不再需要手动的编写 Mock 对象，而是通过 `mockgen` 工具生成 Mock 对象，这样就可以大大提高测试代码的编写效率。

## 引用包 Mock

在单元测试中，我们经常会遇到被测试代码直接调用引用包方法的情况，举个例子，比如我们经常使用 Go 内置的 `os` 包进行文件读写，如果真实地去调用 `os` 包的方法，那么我们就需要在测试中构造测试文件，然后再对测试文件进行清理，这样会增加测试代码的复杂度。我们更希望可以直接 Mock `os` 包中的方法，这样就可以避免对文件系统的依赖，同时可以更加灵活地控制返回值。

下面我们就来介绍一下如何 Mock 引用包的方法，首先我们来看下被测试代码：

```go
package foo

import "os"

func GetEnvVariable() string {
    value := os.Getenv("MY_ENV_VAR")
    if value == "" {
        return "default"
    }
    return value
}
```

被测试代码中直接使用 `os` 包的 `Getenv` 方法获取环境变量，如果我们要 Mock `os` 包中的 `Getenv` 方法，我们需要对这个方法进行一些改造，修改后的代码如下：

```go
package foo

import "os"

var getenv = os.Getenv

func GetEnvVariable() string {
    value := getenv("MY_ENV_VAR")
    if value == "" {
        return "default"
    }
    return value
}
```

我们将 `os.Getenv` 方法赋值给了一个变量 `getenv`，这样我们就可以在测试代码中修改 `getenv` 的值，从而实现 Mock `os` 包中的 `Getenv` 方法。下面我们来看下测试代码：

```go
package foo

import (
    "testing"

    "github.com/stretchr/testify/assert"
)

func TestGetEnvVariable(t *testing.T) {
    oldGetenv := getenv
    defer func() { getenv = oldGetenv }()

    getenv = func(key string) string {
        if key == "MY_ENV_VAR" {
            return "mock_value"
        }
        return ""
    }

    result := GetEnvVariable()
    assert.Equal(t, "mock_value", result)
}
```

- 在测试函数中，我们首先保存了原始的 `getenv` 方法
- 然后通过给 `getenv` 赋值的方式对 `os.Getenv` 方法进行 Mock
- 最后调用 `GetEnvVariable` 方法，验证返回值是否符合预期

其实对于 `os.getenv` 的测试，我们也可以通过 `os.setenv` 方法来设置期望值，但我们现在不是讨论如何让这个测试案例通过，而是如何 Mock 引用包的方法。这种方法无需引用任何第三方库，只需要对被测试代码做一点简单的修改，就可以实现 Mock 引用包的目的，如果你遇到了引用其他包的情况，可以通过这种方式来进行 Mock。

## 数据库 Mock

对于数据库的 Mock， 如果使用之前提到的 Testify Mock 或 GoMock 往往力不从心，你会发现当你写了一大堆 Mock 逻辑后，代码不是编译不通过就是运行起来各种报错。这个时候我们需要更专业的数据库 Mock 工具，比如 [go-sqlmock](https://github.com/DATA-DOG/go-sqlmock)。

go-sqlmock 是一个用于 Go 语言的 SQL 驱动模拟库，旨在测试中模拟真实数据库交互，支持事务、多连接、上下文和命名 SQL 参数，无需修改源代码，且不依赖任何第三方库，它能够模拟任何 SQL 驱动方法的行为，并具有严格的预期顺序匹配。

下面我们来看下如何使用 go-sqlmock 对数据库进行 Mock，首先我们来看下被测试代码：

```go
package foo

import (
	"context"
	"errors"

	"github.com/org/proj/server/db"
	"github.com/org/proj/server/model"

	"gorm.io/gorm"
)

type (
	userRepo struct {
		Instance *db.Instance
	}
)

func (ur *userRepo) FindUser(c context.Context, id uint64) (*model.User, error) {
	user := new(model.User)
	if err := ur.Instance.Conn(c).
		Table(model.TabNameUser()).
		Where("id = ?", id).
		First(user).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, errors.New("user not found")
		}
		return nil, err
	}

	return user, nil
}
```

- 被测试代码中的 `FindUser` 方法是一个查询用户信息的方法
- 它依赖于项目中的 `db.Instance` 对象，这个对象是一个 PostgreSQL 数据库连接对象
- 该方法使用 `grom` 库以 ORM 方式查询数据库信息，简化了 SQL 查询的操作
- 转换成 SQL 语句大概是这样：`SELECT * FROM users WHERE id = ?`

下面我们来看下如何创建一个数据库 Mock 对象：

```go
package foo

import (
	"database/sql"
	"testing"

	"github.com/DATA-DOG/go-sqlmock"
	"gorm.io/driver/postgres"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"
)

func DbMock(t *testing.T) (*sql.DB, *gorm.DB, sqlmock.Sqlmock) {
	sqldb, mock, _ := sqlmock.New()
	dialector := postgres.New(postgres.Config{
		Conn:       sqldb,
		DriverName: "postgres",
	})
	gormdb, _ := gorm.Open(dialector, &gorm.Config{
		Logger: logger.Default.LogMode(logger.Info),
	})
	return sqldb, gormdb, mock
}
```

- `DbMock` 方法使用 `sqlmock.New()` 方法创建 `sqldb` 和 `mock` 对象
- `sqldb` 对象主要用来模拟数据库，`mock` 对象用来设置预期的查询和结果
- 使用 `gorm.Open` 方法创建一个 PostgreSQL 数据库连接对象 `gormdb`

接下来我们来看下测试代码：

```go
package foo

import (
	"testing"

	"github.com/org/proj/server/db"

	"github.com/DATA-DOG/go-sqlmock"
	"github.com/stretchr/testify/assert"
	"gorm.io/gorm"
)

func TestFindUserSuccess(t *testing.T) {
	sqldb, gormdb, mock := DbMock(t)
	defer sqldb.Close()

	testName := "user1"
	testOrg := "org1"
	rows := sqlmock.NewRows([]string{"id", "name", "organization"}).AddRow(1, testName, testOrg)
	mock.ExpectQuery(`SELECT`).WillReturnRows(rows)

	ctx := context.TODO()
	repo := &userRepo{
		Instance: &db.Instance{DB: gormdb},
	}
	user, err := repo.FindUser(ctx, 1)

	assert.NoError(t, err)
	assert.Equal(t, testName, user.Name)
	assert.Equal(t, testOrg, user.Organization)
}
```

- 在测试函数中，我们首先调用 `DbMock` 方法创建返回几个数据库 Mock 对象
- 然后使用 `sqlmock` 设置数据库查询的预期结果，我们使用用户表的 3 个字段设置了一条记录
- 再使用 `mock` 对象模拟查询语句的执行结果，这里的查询语句使用了正则表达式的匹配方式，也就是说，这里的 `SELECT` 可以匹配任何以 `SELECT` 开头的 SQL 语句，包括 `SELECT * FROM users WHERE id = ?`
- 使用模拟的 `gormdb` 实例化 `userRepo` 对象
- 最后调用 `FindUser` 方法，验证返回值是否符合预期

## Http 请求 Mock

在 Web 开发中，我们经常会遇到对外部 HTTP 服务的调用，比如调用第三方 API、调用微服务等。在单元测试中，我们不可能对这些外部服务进行真实的调用，这样会使得单元测试变得缓慢且不稳定，我们需要对这些 HTTP 请求进行 Mock，这样就可以模拟外部服务的行为，从而使得单元测试更加高效和可靠。

[httptest](https://pkg.go.dev/net/http/httptest) 是 Go 语言标准库中提供的一个 HTTP 服务测试工具，它可以模拟 HTTP 请求和响应，用于测试 HTTP 服务的功能和性能。httptest 包中的 `NewRequest` 和 `NewRecorder` 方法可以模拟 HTTP 请求和响应，我们可以使用这两个方法来模拟 HTTP 请求和响应，从而实现对 HTTP 服务的 Mock。

下面我们来看下如何使用 httptest 对 HTTP 请求进行 Mock，以常用的 Web 开发框架 [Gin](https://github.com/gin-gonic/gin) 为例，下面是一个 Gin 的 Controller 例子，Controller 中有一个 `Login` 方法，用于处理用户登录请求：

```go
package foo

import (
	"net/http"

	"github.com/org/proj/server/dto"

	"github.com/gin-gonic/gin"
)

type (
	controller struct {
		service FooService
	}
)

func (re *controller) Login(c *gin.Context) {
	req := new(dto.ReqLogin)
	if err := c.BindJSON(req); err != nil {
		c.Error(err)
		return
	}

	resp, err := re.service.Login(c, *req)
	if err != nil {
		c.Error(err)
		return
	}

	c.JSON(http.StatusOK, resp)
}
```

- `Login` 方法从请求 Body 中获取数据并转换为 `ReqLogin` 对象
- 然后调用 service 中的 `Login` 方法处理登录逻辑
- 最后将 server 方法的返回结果序列化为 JSON 返回给客户端

下面我们来看下如何对 Controller 的 `Login` 方法进行单元测试，这里我们需要使用 GoMock 对 `service` 进行 Mock，同时使用 httptest 对 HTTP 请求进行 Mock：

```go
package foo

import (
	"bytes"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/org/proj/server/dto"

	"github.com/gin-gonic/gin"
	"github.com/golang/mock/gomock"
	"github.com/stretchr/testify/assert"
)

func TestResourceLoginSuccess(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	jsonBody := []byte(`{ "account": "foo", "password": "baz" }`)
	req := httptest.NewRequest("POST", "/login", bytes.NewBuffer(jsonBody))
	req.Header.Set("Content-Type", "application/json")

	w := httptest.NewRecorder()
	ctx, _ := gin.CreateTestContext(w)
	ctx.Request = req

	mockService := NewMockOAuthService(ctrl)
	expectedResp := &dto.RespLogin{
		Account:      "test-account",
		Organization: "test-org",
	}
	mockService.EXPECT().Login(ctx, gomock.Any()).Return(expectedResp, nil)

	cd := &controller{
		service: mockService,
	}

	cd.Login(ctx)

	assert.Equal(t, http.StatusOK, ctx.Writer.Status())
	assert.Nil(t, ctx.Errors)

	resp := &dto.RespLogin{}
	err := json.Unmarshal(w.Body.Bytes(), resp)
	assert.Nil(t, err)
	assert.Equal(t, expectedResp, resp)
}
```

- 首先使用 `httptest.NewRequest` 方法创建一个 URL 为 `/login` 的 POST 请求，请求 Body 为一个我们设置好的 JSON 数据
- 然后使用 `httptest.NewRecorder` 方法创建一个 Response 对象，用于保存请求的返回结果，使用 `gin.CreateTestContext` 方法创建一个测试上下文对象，将 Request 对象赋值给上下文对象
- 接着使用 GoMock 对 `service` 的 `Login` 方法进行 Mock，设置预期的返回值
- 调用 Controller 的 `Login` 方法，传入上下文对象
- 在验证阶段，先验证返回状态码和 `ctx.Errors` 是否正确
- 最后验证被测方法中 `c.JSON(http.StatusOK, resp)` 返回的 JSON 数据是否符合预期，这个 JSON 在原方法中被添加到了 `ctx.Writer` 中，我们可以通过 `w.Body.Bytes()` 获取到这个 JSON 数据

## 总结

今天我们介绍了在 Golang 中对不同测试对象进行 Mock 的 5 种方法，包括常用的测试工具库 Testify Mock 和 GoMock，以及如何 Mock 引用包、数据库和 HTTP 请求。通过这些 Mock 技术，我们能够灵活替换外部依赖，定制其行为和返回值，确保测试环境的可控性。掌握了这些 Mock 技术，以后无论是模拟任何数据或方法，都能让你的单元测试更加高效和可靠。

关注我，一起学习最新的开发编程新技术，欢迎交流，如果你有什么想问想说的，欢迎在评论区留言。
