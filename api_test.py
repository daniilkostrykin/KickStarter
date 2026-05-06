import requests

BASE_URL = "http://localhost:8080"

print("🔍 Начинаем аудит пагинации KickStarter API...\n")

# 1. Проверяем REST API
rest_endpoints = [
    "/api/projects?page=0&size=2",
    "/api/rewards?page=0&size=2",
    "/api/users?page=0&size=2",
    "/api/pledges?page=0&size=2"
]

print("=== Проверка REST API ===")
for endpoint in rest_endpoints:
    url = f"{BASE_URL}{endpoint}"
    try:
        response = requests.get(url)
        name = endpoint.split('?')[0]

        if response.status_code == 200:
            data = response.json()
            # Убеждаемся, что Spring HATEOAS вернул объект page
            if "page" in data and "_links" in data:
                print(f"✅ {name:15} : Пагинация ОК! (Найдено элементов: {data['page']['totalElements']})")
            else:
                print(f"⚠️  {name:15} : Ответ 200, но структуры пагинации нет! Возможно метод возвращает List, а не PagedModel.")
        elif response.status_code == 404:
            print(f"❌ {name:15} : Эндпоинт не найден (ошибка 404). Контроллер отсутствует?")
        else:
            print(f"❌ {name:15} : Ошибка сервера {response.status_code}")
    except requests.exceptions.ConnectionError:
         print(f"❌ Ошибка соединения! Сервер на {BASE_URL} запущен?")
         exit()

# 2. Проверяем GraphQL API
print("\n=== Проверка GraphQL API ===")
graphql_url = f"{BASE_URL}/graphql"
# Тестируем пагинацию по твоей новой схеме
graphql_query = """
query TestPagination {
  projects(page: 0, size: 2) {
    totalElements
    pageInfo { totalPages last }
    content { id title }
  }
  pledges(page: 0, size: 2) {
    totalElements
    pageInfo { pageNumber }
    content { pledgeId amount }
  }
}
"""

try:
    response = requests.post(graphql_url, json={'query': graphql_query})
    if response.status_code == 200:
        data = response.json()
        if "errors" in data:
            print("❌ GraphQL вернул ошибки:")
            for err in data["errors"]:
                print(f"   - {err['message']}")
        else:
            p_data = data['data']['projects']
            pl_data = data['data']['pledges']
            print("✅ GraphQL        : Пагинация ОК!")
            print(f"   - Проектов: всего {p_data['totalElements']}, страниц {p_data['pageInfo']['totalPages']}, последняя: {p_data['pageInfo']['last']}")
            print(f"   - Взносов : всего {pl_data['totalElements']}, текущая страница: {pl_data['pageInfo']['pageNumber']}")
    else:
         print(f"❌ GraphQL ответил статусом {response.status_code}")
except Exception as e:
    print(f"❌ Ошибка при запросе GraphQL: {e}")