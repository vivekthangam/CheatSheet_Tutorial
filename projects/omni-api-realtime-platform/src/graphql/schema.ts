import fs from 'fs';
import path from 'path';
import { buildSchema, GraphQLSchema } from 'graphql';
import { resolvers, createGraphQLContext } from './resolvers';

let cachedSchema: GraphQLSchema | null = null;

export function getGraphQLSchema(): GraphQLSchema {
  if (cachedSchema) return cachedSchema;

  const schemaPath = path.resolve(__dirname, '../../specs/schema.graphql');
  const typeDefs = fs.readFileSync(schemaPath, 'utf8');

  cachedSchema = buildSchema(typeDefs);
  return cachedSchema;
}

export { resolvers, createGraphQLContext };
