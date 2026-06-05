# 08 Mapper Rules

## Purpose
Prevent mapper misuse in Mentor X backend.

## Core Rules
- Mappers only convert `Entity <-> DTO` or similar simple model transformations.
- Mappers must be deterministic and side-effect free.
- Mappers must not contain business logic.
- Mappers must not call repositories or services.

## Allowed Mapper Responsibilities
- copy fields
- format nested DTO structure
- flatten or expand simple model shape
- map enums and null-safe values

## Forbidden Mapper Responsibilities
- permission checks
- ownership checks
- wallet calculations
- escrow calculations
- contract lifecycle decisions
- status transitions
- translated UI text
- repository queries
- current-user inspection

## Permission Flag Rule
- If a response needs permission flags such as `canCancel`, `canComplete`, or `canRespond`, calculate them in the service layer.
- Mapper may only copy those values into a response model.

## Financial Rule
- Mappers must never calculate final MXC balances, escrow totals, or payout eligibility.
- Financial numbers returned by the mapper must come from service-approved domain state.

## Example
Good:

```java
response.setCanComplete(contractPolicy.canComplete(contract, actor));
return contractMapper.toResponse(contract, response);
```

Bad:

```java
if (currentUserIsClient(contract)) {
    response.setCanComplete(true);
}
response.setAmountInEscrow(calculateEscrow(contract));
```

## Do
- Keep mappers boring.
- Keep mapping rules obvious and testable.
- Push non-trivial decisions into services.

## Do Not
- Hide business policy inside generated mapper helpers.
- Reach into security context from a mapper.
- Use mapper hooks to mutate persistence state.

## MVP Scope
- Safe field transformation without policy leakage.

## Future Scope / TODO
- If mapper count grows, define shared conventions for null handling and nested projection only.
