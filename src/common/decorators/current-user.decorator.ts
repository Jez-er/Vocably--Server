import { createParamDecorator, ExecutionContext } from '@nestjs/common';
import { GqlExecutionContext } from '@nestjs/graphql';

type GraphQLContextWithRequest = {
  req?: {
    user?: unknown;
  };
};

export const CurrentUser = createParamDecorator(
  (_data: unknown, context: ExecutionContext): unknown => {
    const ctx = GqlExecutionContext.create(context);
    const request = ctx.getContext<GraphQLContextWithRequest>().req;

    return request?.user;
  },
);
