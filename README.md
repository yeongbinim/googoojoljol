## 📖 목차

1. [소개](#-소개)
2. [Architecture + 기술 스택](#-architecture--기술-스택)
3. [문제 해결](#-문제-해결)
4. [Members](#-Members)

## 📝 소개

이 프로젝트는 하나의 온전한 애플리케이션 개발에 필요한 핵심 기술을 집중적으로 학습하고 구현하기 위한 프로젝트입니다. </br>

```markdown
🔑 Core Technologies

1. 동시성 제어
2. 인덱싱
3. 캐싱
```

</br><img src="https://img.shields.io/badge/프로젝트 기간-2025.01.31~2025.02.07-green?style=flat&logo=&logoColor=white" />

<br />

## 🏗️ Architecture & 기술 스택

<div align="center"><img width="700" alt="image" src="https://github.com/user-attachments/assets/58c4c281-e31b-405c-bb16-78771a808ae8" /></div>
<div align="center">
  <img src="https://img.shields.io/badge/Java-007396?style=flat-square&logo=OpenJDK&logoColor=white">&nbsp;
  <img src="https://img.shields.io/badge/Spring-6DB33F?style=flat-square&logo=spring&logoColor=white">&nbsp;
  <img src="https://img.shields.io/badge/Spring Boot-6DB33F?style=flat-square&logo=springboot&logoColor=white">&nbsp;
  <img src="https://img.shields.io/badge/Gradle-02303A?style=flat-square&logo=gradle&logoColor=white">&nbsp;
  <img src="https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white">&nbsp;
  <img src="https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=redis&logoColor=white">&nbsp;
  <img src="https://img.shields.io/badge/Amazon AWS-232F3E?style=flat-square&logo=amazonaws&logoColor=white">&nbsp;
  <img src="https://img.shields.io/badge/Amazon EC2-FF9900?style=flat-square&logo=amazonec2&logoColor=white">&nbsp;
  <img src="https://img.shields.io/badge/Amazon RDS-527FFF?style=flat-square&logo=amazonrds&logoColor=white">&nbsp;
  <br/>
  <img src="https://img.shields.io/badge/IntelliJ IDEA-000000?style=flat-square&logo=IntelliJ IDEA&logoColor=white">&nbsp;
  <img src="https://img.shields.io/badge/Github-181717?style=flat-square&logo=github&logoColor=white">&nbsp;
  <img src="https://img.shields.io/badge/git-F05032?style=flat-square&logo=git&logoColor=white">&nbsp;  
</div>


<br />

## 🧠 문제 해결

<details>
  <summary>1. 🔒 동시성 제어 - 쇼핑몰 조회수 동시성 제어 (Lock 적용)
</summary>
  <br />

## 쇼핑몰 조회수 동시성 제어 (Lock 적용)

쇼핑몰 조회수 증가에서 발생하는 동시성 이슈를 해결한 과정에 대해서 정리했습니다.

Synchronized -> DB Lock(Pessimistic, Optimistic) -> Redis 분산 Lock (Lettuce, Redisson) 모두 적용하여 문제를 해결해보고 비교 분석한 과정을 다룹니다.

<br/>

### 1️⃣ **Synchronized 사용 (단일 서버 환경)**

- 단일 프로세스에서는 동작하지만 **멀티 서버 환경에서는 보장되지 않음**
- 동시성 문제 해결 가능하지만, **성능 저하 발생** (병렬 처리 제한)

<br/>

### 2️⃣ **DB Lock 활용 (Pessimistic vs. Optimistic)**

| 방식                             | 특징                                                   | 장점             | 단점                           |
| -------------------------------- | ------------------------------------------------------ | ---------------- | ------------------------------ |
| **비관적 락 (Pessimistic Lock)** | DB 자체에서 트랜잭션 락 사용 (`SELECT ... FOR UPDATE`) | 충돌 방지 효과적 | 락으로 인해 성능 저하 가능     |
| **낙관적 락 (Optimistic Lock)**  | `@Version` 필드 활용 (충돌 발생 시 재시도)             | 병렬 처리 가능   | 충돌이 잦으면 재시도 비용 발생 |

<br/>

### 3️⃣ **Redis 기반 분산 락 (Lettuce vs. Redisson)**

| 방식         | 특징          | 장점                  | 단점                               |
| ------------ | ------------- | --------------------- | ---------------------------------- |
| **Lettuce**  | `SET NX` 방식 | 간단한 구현           | Spin Lock 방식으로 Redis 부하 가능 |
| **Redisson** | Pub-Sub 기반  | 효율적인 락 해제 감지 | 추가 라이브러리 필요               |

<br/>

### 🧐 결론

- **Synchronized는 성능 저하 및 단일 서버 환경에서만 동작**하므로 가급적 사용을 피해야 한다.

- **DB 서버가 한 대라면 비관적 락(Pessimistic Lock)이 적절하지만**, 데드락 및 성능 저하 문제를 고려해야 한다.
  - 낙관적 락(Optimistic Lock)은 추가 설정이 많아 복잡하고, 빈번한 충돌이 예상된다면 비관적 락을 선택하는 것이 일반적이다.

- **DB 서버가 여러 대라면 분산 락이 필요하며, Redisson을 활용하는 것이 더 효율적**이다.
  - Lettuce는 Spin Lock 방식으로 Redis 부하가 커질 수 있어 주의가 필요하다.

<br/>

### 🔗 더 자세한 내용 보기

[📌 상세한 구현 과정과 코드 설명이 궁금하다면?](/dev_log/Lock으로_동시성_이슈_해결하기.md)

<hr/>

</details>
<details>
  <summary>2. 📀 인덱싱 - 쇼핑몰 부분 일치 검색 최적화: 속도 7배 개선</summary>
  <br />
  
> **문제점**: QueryDSL에서 `MATCH ... AGAINST` 구문을 지원하지 않는다. </br>
> **개선**: `CustomFunction` + `NGRAM` 기반 인덱스 사용 </br>

![Before](https://github.com/user-attachments/assets/ac77d312-6f91-4b9b-8322-432622ecc92b) </br>
![After](https://github.com/user-attachments/assets/4f4f10e9-8651-4ae5-a1bd-739ed2f3ab4c) </br>


> **개선 결과** : 137 ms -> 20 ms 약 7배 속도 개선
  <hr>
</details>
<details>
    <summary>3. 💾 캐싱 - 🏆 쇼핑몰 랭킹 최적화: DB에서 Redis까지</summary>
    <br />

## 🏆 쇼핑몰 랭킹 최적화: DB에서 Redis까지

쇼핑몰 순위를 실시간으로 빠르게 조회하기 위해 DB 조회 → 캐싱 적용 → Redis 최적화까지 단계별 접근법을 적용했습니다. 
이 과정에서 조회 속도, 데이터 정합성, 동시성 문제를 해결하는 방법을 정리했습니다.

## 🔎 개요

쇼핑몰 랭킹을 조회하는 API의 성능을 최적화하는 과정에서 캐싱과 데이터 구조 설계가 중요한 이유를 정리했습니다.

* 초기 방식: 단순히 DB에서 데이터를 조회
* 문제점: 트래픽 증가 시 DB 부하가 심해지고 성능 저하 발생
* 해결책: Spring Cache, Redis를 도입하여 조회 속도를 개선
* 최적화: Redis Sorted Set + Hash를 활용하여 성능 향상 & 동시성 문제 해결

## 𓊍 단계별 접근 방식

### ✅ V0. 기본 DB 조회
* DB에서 `ORDER BY viewCount DESC LIMIT 100`을 사용해 랭킹 조회
* 장점: 최신 데이터를 반영할 수 있음
* 단점: 트래픽 증가 시 DB 부하 심화 → 성능 저하 발생

### ✅ V1. 캐싱 적용 (`Spring Cache` & `Redis`)
* V1-1: Spring Cache (로컬 캐시) 적용 → 속도 개선, 서버 간 데이터 불일치 가능
* V1-2: Redis 적용 → 분산 캐시로 성능 향상. TTL을 활용해 최신 데이터 유지
* V1-3: `@Cacheable` & `@CachePut` 활용 → 조회수 어뷰징 방지

### ⚠️ 문제점:
* 캐시된 데이터는 최신 상태가 아닐 가능성이 있음
* 조회수가 증가할 때 동시성 문제 발생 가능

### ✅ V2. `Redis` 기반 최적화 (`Sorted Set` + `Hash`)
* 기존 리스트 기반 캐싱의 문제: 데이터 삽입·삭제 시 순서 변경에 따른 연산 비용 증가 `(O(N))`
* 해결책: `Redis Sorted Set(ZSet)` + `Hash` 사용
* `Sorted Set`: 조회수를 점수(score)로 저장하여 자동 정렬
* `Hash`: 쇼핑몰 정보를 Key-Value 형태로 저장하여 빠른 조회 가능
* 조회수 증가 로직 최적화 → `incrementScore()` 사용
* 정렬 비용 감소 `(O(log N))`

## 🧩 추가 해결 사항:

* 트랜잭션 적용 (`MULTI/EXEC`):
  * `Redis`가 싱글 스레드 모델이기에 복합 연산시 트랜잭션을 활용하면 동시성 문제 해결 가능
* 벌크 업데이트 (`CASE-WHEN` 활용) → 10분 단위로 DB에 일괄 반영

## 🛠️ 주요 기술 & 문제 해결 전략

| 문제              | 해결방법                                     |
|-----------------|------------------------------------------|
| DB 부하 증가        | `Redis` 캐싱 도입 (`Spring Cache` & `Redis`) |
| 리스트 기반 저장의 비효율성 | `Sorted Set` + `Hash` 조합으로 개선            |
| 조회수 증가 시 동시성 문제 | `Sorted Set` 활용 및 Redis 자체 Atomic 연산 활용  |
| 캐싱된 데이터 정합성 문제  | TTL 설정 및 10분 단위 벌크 업데이트                  |

## 🔗 더 자세한 내용 보기

[📌 상세한 구현 과정과 코드 설명이 궁금하다면?](/dev_log/dev-log-suk-v2.md)
</details>
<br />


## 👨‍👩‍👧‍👦 Members

<table align="center">
    <thead>
        <tr>
            <th>👑 팀장</th>
            <th>팀원</th>
            <th>팀원</th>
            <th>팀원</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td align="center"><a href="https://github.com/yeongbinim"><img src="https://github.com/yeongbinim.png" width="100px;" alt=""/></a></td>
            <td align="center"><a href="https://github.com/freedrawing"><img src="https://github.com/freedrawing.png" width="100px;" alt=""/></a></td>
            <td align="center"><a href="https://github.com/sinwoo-kim"><img src="https://github.com/sinwoo-kim.png" width="100px;" alt=""/></a></td>
            <td align="center"><a href="https://github.com/ju-young0"><img src="https://github.com/ju-young0.png" width="100px;" alt=""/></a></td>
        </tr>
        <tr>
            <td align="center">임영빈</td>
            <td align="center">강성욱</td>
            <td align="center">김신우</td>
            <td align="center">윤주영</td>
        </tr>
    </tbody>
</table>

