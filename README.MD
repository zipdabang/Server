# zipdabang server

<img src="https://github.com/zipdabang/Server/assets/60510921/42841eda-59d0-4aeb-8707-fe1889d72ce2" width="300px">


zipdabang 서버 리포지토리 입니다

# Developers 🎉
| 역할     | 이름                                                                                                                       | 역할                            | Contact                                                                                                                                                                       |
|--------|--------------------------------------------------------------------------------------------------------------------------|-------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Server  | <img src="https://github.com/zipdabang/Server/assets/60510921/d41a543f-88d8-4d87-9f31-f23ec15b5f83" width="100px"> <br> 최용욱 | Spring boot 리드👑 , 개발, DevOps | 📒노션 페이지📒 <br>https://shine-hardcover-c55.notion.site/5da286d0633e4ee4acb6b6bd6bbd0224?pvs=4 📨Email📨<br> younguk86@gmail.com <br> ⚔️Github⚔️<br>https://github.com/CYY1007 |
| Server    | <img src="https://github.com/zipdabang/Server/assets/60510921/3177e9d4-af22-4ae1-b0f6-338d8c2c1e28" width="100px"> <br> 양효빈 | Spring boot 개발, DevOps        |  📒노션 페이지📒 <br> 📨Email📨 <br> qls0786@naver.com <br> ⚔️Github⚔️<br>https://github.com/HyoBN                                                                                                                                                                               |
| Server    | <img src="https://github.com/zipdabang/Server/assets/60510921/8b0a2288-95b2-4ec6-96a3-1718f1342413" width="100px"> <br> 김예지 | DevpOps 리드👑, Spring boot 개발  | 📒노션 페이지📒 <br>https://hanvp.notion.site/838545d7199b4289aa680230740ef0ef 📨Email📨 <br> nwactris@naver.com <br> ⚔️Github⚔️<br>https://github.com/Hanvp                       |





# Technology Stack
<b>사용된 기술 스택, 및 배포 툴</b>

| name                     | logo | usage                           |
|--------------------------|------|---------------------------------|
| NGINX                    |  ![image](https://github.com/zipdabang/Server/assets/60510921/7b3d802f-f0a2-4127-8397-f9ae5e84b04f) | Web Server 구축                   |
| Spring Boot (2.7, JDK11) |  ![image](https://github.com/zipdabang/Server/assets/60510921/522bbfe5-4526-4df2-b167-b7203aa883aa) | Web Applicatoin Server 구축       |
| Spring Data JPA          | ![image](https://github.com/zipdabang/Server/assets/60510921/d183ddb4-d25c-448e-8bc0-28b798ee4c4b)  | ORM, DBMS와 통신을 위해 사용            |
| MySQL                    |  ![image](https://github.com/zipdabang/Server/assets/60510921/cc761fe5-6755-469a-9d9c-fb1b77c4108d) | for DBMS                        |
| Spring Security          |  ![image](https://github.com/zipdabang/Server/assets/60510921/f9cda208-0954-4e9d-9092-8520d5430178) | JWT, 권한 등 보안 설정을 위해 사용          |
| Spring Batch             |  ![image](https://github.com/zipdabang/Server/assets/60510921/0bfb74a1-55fc-4e3b-b89b-f462b6048525) | 클라이언트의 요청과 별개로 데이터를 처리하기 위함     |
| AWS Elastic Beanstalk    |  ![image](https://github.com/zipdabang/Server/assets/60510921/3b381aff-443e-4090-8aeb-bfdb6533684f)| 배포의 대상이 되는 EC2를 편하게 관리          |
| AWS S3                   |  ![image](https://github.com/zipdabang/Server/assets/60510921/2b507f08-bea0-42b3-9783-23a0fadbda54)    | 클라이언트가 업로드하는 Multipart file 업로드 |
| AWS Route53              |  ![image](https://github.com/zipdabang/Server/assets/60510921/257b2154-a621-41c9-a5bb-efe6b64349d8)    | Domain Name System 관리           |
| AWS ElastiCache          |  ![image](https://github.com/zipdabang/Server/assets/60510921/000428d6-0caf-48d3-89cb-ef3a9b566385)    | Redis 클러스터를 관리                  |
| AWS RDS                  |  ![image](https://github.com/zipdabang/Server/assets/60510921/c2bdee1b-adcb-48fd-84f7-4d30d73042af)    | DB를 관리                          |
| Redis                    | ![image](https://github.com/zipdabang/Server/assets/60510921/f087c4ab-95f6-4884-aba4-81e5e0968349)     | for in memory cache             |
| Github action            |  ![image](https://github.com/zipdabang/Server/assets/60510921/a9f0ebb8-78a1-465f-9652-b70eac08857b)    | for CI/CD                       |

## 협업 전략

## 개발 프로세스

1. **이슈 생성**
    - 먼저 작업할 내용에 대한 이슈를 생성합니다.

2. **Jira를 사용하는 경우**
    - Jira를 사용하는 경우 다음 단계를 따릅니다:
        1. Jira에서 이슈용 브랜치를 생성합니다.
        2. Jira에서 생성한 브랜치를 해당 이슈에 연결합니다.
        3. 로컬에서 "develop" 브랜치에서 새로운 브랜치를 만듭니다.
            - 주의: 새로운 브랜치를 만들기 전에 `git pull origin develop`을 실행합니다.
        4. 브랜치를 원격 저장소에 푸시한 후 코드 리뷰를 진행합니다.
        5. 코드 리뷰 후에 브랜치를 병합합니다.

3. **Jira를 사용하지 않는 경우**
    - Jira를 사용하지 않는 경우 다음 단계를 따릅니다:
        1. 이슈에서 작업 브랜치를 생성합니다.
        2. 로컬에서 "develop" 브랜치에서 새로운 브랜치를 만듭니다.
            - 주의: 새로운 브랜치를 만들기 전에 `git pull origin develop`을 실행합니다.
        3. 브랜치를 원격 저장소에 푸시한 후 코드 리뷰를 진행합니다.
        4. 코드 리뷰 후에 브랜치를 병합합니다.

이 프로세스는 개발 워크플로를 효율화하고 코드 품질을 병합 전에 보장합니다.


### CI/CD 전략

develop : 개발 서버 배포용, 이 브랜치로 merge 시 github action을 통해
개발 인프라 서버로 자동 배포
이 브랜치로의 merge는 기능 개발 브랜치 5종류에서 merge

release : 실제 서비스를 위한 배포용, 이 브랜치로 merge 시 github action을 통해
릴리즈 인프라 서버로 자동 배포
이 브랜치로의 merge는 develop에서 merge

### 브랜치 전략 after 2023/10/17

| 브랜치 이름                           | 사용 목적                                                             |
|----------------------------------|-------------------------------------------------------------------|
| main 👑                          | 초기 세팅, 릴리즈 용도로 사용                                                 |
| develop 🏗️                      | 개발용 배포 서버 대상 브랜치                                                  |
| feature/N ✨                      | 새로운 기능 개발을 하기 위한 브랜치, N은 이슈 번호                                    |
| refactor/N :recycle:             | 이미 있는 기능에 대한 수정을 위한 브랜치, N은 이슈 번호                                 |
| bug/N      :bug:                 | 버그를 고치기 위한 브랜치, N은 이슈 번호                                          |
| docs/N     :memo:                | swagger 명세, 혹은 주석 등의 작업만 하는 브랜치, N은 이슈 번호                         |
| ci/N       :construction_worker: | 배포 관련 작업을 위한 브랜치, N은 이슈 번호                                        |
| WKZB-N + 위의 5개 전략       🪄       | jira 사용시 jira와 연동을 위한 브랜치, N은 지라 TASK번호  ex) WKZB-411-feature/130 |

### 커밋컨벤션

| 태그이름                       | 내용                                          |
|----------------------------|---------------------------------------------|
| :sparkles: `Feat`          | 새로운 기능을 추가할 경우                              |
| :bug:`Fix `                | 버그를 고친 경우                                   |
| :bug:`!HOTFIX`             | 급하게 치명적인 버그를 고쳐야하는 경우                       |
| `Style`                    | 코드 포맷 변경, 세미 콜론 누락, 코드 수정이 없는 경우            |
| :recycle:`Refactor`        | 코드 리팩토링                                     |
| :memo:`Comment`            | 필요한 주석 추가 및 변경                              |
| :memo:`Docs`	              | 문서, Swagger 를 수정한 경우                        |
| :hammer:`Test`             | 테스트 추가, 테스트 리팩토링(프로덕션 코드 변경 X)              |
| `Chore`	                   | 빌드 태스트 업데이트, 패키지 매니저를 설정하는 경우(프로덕션 코드 변경 X) |
| `Rename`                   | 파일 혹은 폴더명을 수정하거나 옮기는 작업만인 경우                |
| `Remove`                   | 파일을 삭제하는 작업만 수행한 경우                         |
| :construction_worker: `Ci` | 배포 방식 수정 및 새로 추가                            |
| :green_heart: `Ci`         | 기존 배포 스크립트 수정                               |
